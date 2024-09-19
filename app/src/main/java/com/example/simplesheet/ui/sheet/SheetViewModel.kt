package com.example.simplesheet.ui.sheet

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.simplesheet.data.AccountService
import com.example.simplesheet.data.Item
import com.example.simplesheet.data.Sheet
import com.example.simplesheet.data.SheetFontSize
import com.example.simplesheet.data.SheetSortType
import com.example.simplesheet.data.SheetView
import com.example.simplesheet.data.StorageService
import com.example.simplesheet.ui.AppViewModel
import com.example.simplesheet.ui.dashboard.DashboardDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class SheetViewModel @Inject constructor(
    private val accountService: AccountService,
    private val storageService: StorageService,
    private val savedStateHandle: SavedStateHandle,
): AppViewModel(accountService) {

    companion object {
        const val HISTORY_SIZE = 5
    }

    private val _sheetId = MutableStateFlow("-1")

    private val _sheet = MutableStateFlow(Sheet())
    val sheet: StateFlow<Sheet> = _sheet.asStateFlow()

    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _noteText = MutableStateFlow("")
    val noteText: StateFlow<String> = _noteText.asStateFlow()
    private val _noteHistory = MutableStateFlow(ArrayDeque<String>())
    val noteHistory: StateFlow<ArrayDeque<String>> = _noteHistory.asStateFlow()
    private val _noteFuture = MutableStateFlow(ArrayDeque<String>())
    val noteFuture: StateFlow<ArrayDeque<String>> = _noteFuture.asStateFlow()

    private val _selectedItem = MutableStateFlow(Item())
    val selectedItem: StateFlow<Item> = _selectedItem.asStateFlow()

    private val _dialogState = MutableStateFlow(SheetDialogState.None)
    val dialogState: StateFlow<SheetDialogState> = _dialogState.asStateFlow()

    private val _editPermission = MutableStateFlow(false)
    val editPermission: StateFlow<Boolean> = _editPermission.asStateFlow()

    fun softReset(onSignedOut: () -> Unit) {
        readSheetId()
        checkEditPermission()

        _requestPending.value = true
        viewModelScope.launch {
            reloadSheet()
            _requestPending.value = false
        }

        observeAuthenticationState(onSignedOut)
    }

    fun readSheetId() {
        _sheetId.update { checkNotNull(savedStateHandle["sheetId"]) }
    }

    suspend fun reloadSheet() {
        _sheet.value = storageService.readSheet(_sheetId.value) ?: Sheet()
        _items.value = storageService.readAllItems(_sheetId.value)
        _noteText.value = _sheet.value.text

        _items.value = when (_sheet.value.sortType) {
            SheetSortType.MainText -> _items.value.sortedBy{
                if (it.text.isBlank()) "z${it.text}" else "a${it.text}"
            }
            SheetSortType.Label -> _items.value.sortedBy {
                if (it.label.isBlank()) "z${it.label}" else "a${it.label}"
            }
            SheetSortType.Data -> _items.value.sortedBy {
                if (it.data.isBlank()) "z${it.data}" else "a${it.data}"
            }
            SheetSortType.DateCreated -> _items.value.sortedByDescending { it.dateCreated }
            SheetSortType.DateModified -> _items.value.sortedByDescending { it.dateModified }
        }
    }

    private fun observeAuthenticationState(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            accountService.currentUser.collect { user ->
                if (user == null) onSignedOut()
            }
        }
    }

    fun rememberAndUpdateNoteText(noteText: String) {
        _noteHistory.value.addFirst(_noteText.value)
        if (_noteHistory.value.size > HISTORY_SIZE) _noteHistory.value.removeLast()
        _noteFuture.value.clear()
        _noteText.value = noteText
    }

    fun undoNoteChanges() {
        if (_noteHistory.value.isEmpty()) return
        _noteFuture.value.addFirst(_noteText.value)
        _noteText.value = _noteHistory.value.removeFirst()
    }

    fun redoNoteChanges() {
        if (_noteFuture.value.isEmpty()) return
        _noteHistory.value.addFirst(_noteText.value)
        _noteText.value = _noteFuture.value.removeFirst()
    }

    fun checkEditPermission(): Boolean {
        _editPermission.value = _sheet.value.editableBy.contains(accountService.getUserProfile().gmail)
        return _editPermission.value
    }

    fun getUserGmail(): String {
        return accountService.getUserProfile().gmail
    }

    fun selectItem(index: Int): Item {
        val item = _items.value.getOrElse(index) { return Item() }
        _selectedItem.value = item
        return item
    }

    fun createItem(text: String, label: String, data: String) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.createItem(
                sheet = _sheet.value,
                item = Item(
                    text = text,
                    label = label,
                    data = data,
                ),
            )
            reloadSheet()
            closeDialog()
            _requestPending.value = false
        }
    }

    fun updateItem(text: String, label: String, data: String) {

        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.updateItem(_sheet.value,
                _selectedItem.value.copy(
                    text = text,
                    label = label,
                    data = data,
                )
            )
            reloadSheet()
            closeDialog()
            _requestPending.value = false
        }
    }

    fun updateSheet(title: String, view: SheetView, fontSize: SheetFontSize, maxLines: Int) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.updateSheet(
                _sheet.value.copy(
                    title = title,
                    view = view,
                    fontSize = fontSize,
                    maxLines = maxLines
                )
            )
            reloadSheet()
            closeDialog()
            _requestPending.value = false
        }
    }

    fun updateSheetNoteText(doAfter: () -> Unit = {}) {
        if (_requestPending.value) return
        if (_sheet.value.text == _noteText.value) {
            doAfter()
            return
        }
        _requestPending.value = true
        viewModelScope.launch {
            storageService.updateSheet(
                _sheet.value.copy(
                    text = _noteText.value
                )
            )
            reloadSheet()
            closeDialog()
            _requestPending.value = false
            doAfter()
        }
    }

    fun deleteItem() {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.deleteItem(_sheet.value, _selectedItem.value)
            reloadSheet()
            closeDialog()
            _requestPending.value = false
        }
    }

    fun deleteSheet(onBackRequested: () -> Unit) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.deleteSheet(_sheet.value.id)
            reloadSheet()
            closeDialog()
            _requestPending.value = false
            onBackRequested()
        }
    }

    fun shareSheet(gmail: String, editPermission: Boolean) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {

            val viewableBy = _sheet.value.viewableBy.toMutableSet()
            viewableBy.add(gmail)

            if (!editPermission) {
                storageService.updateSheet(
                    _sheet.value.copy( viewableBy = viewableBy.toMutableList() )
                )

            } else {
                val editableBy = _sheet.value.editableBy.toMutableSet()
                editableBy.add(gmail)

                storageService.updateSheet(
                    _sheet.value.copy(
                        viewableBy = viewableBy.toMutableList(),
                        editableBy = editableBy.toMutableList(),
                    )
                )
            }

            reloadSheet()
            _requestPending.value = false
            openManageUsersDialog()
        }
    }

    fun setUserPermission(gmail: String, editPermission: Boolean) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {

            if (!editPermission) {
                val editableBy = _sheet.value.editableBy.toMutableList()
                editableBy.remove(gmail)

                storageService.updateSheet(
                    _sheet.value.copy(
                        editableBy = editableBy,
                    )
                )

            } else {
                val editableBy = _sheet.value.editableBy.toMutableSet()
                editableBy.add(gmail)

                storageService.updateSheet(
                    _sheet.value.copy(
                        editableBy = editableBy.toMutableList(),
                    )
                )
            }

            reloadSheet()
            _requestPending.value = false
            openManageUsersDialog()
        }
    }

    fun removeUser(gmail: String) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {

            val viewableBy = _sheet.value.viewableBy.toMutableList()
            viewableBy.remove(gmail)
            val editableBy = _sheet.value.editableBy.toMutableList()
            editableBy.remove(gmail)

            storageService.updateSheet(
                _sheet.value.copy(
                    viewableBy = viewableBy,
                    editableBy = editableBy,
                )
            )

            reloadSheet()
            _requestPending.value = false
            openManageUsersDialog()
        }
    }

    fun openCreateItemDialog() { _dialogState.value = SheetDialogState.AddItem }

    fun openEditItemDialog() { _dialogState.value = SheetDialogState.EditItem }

    fun openSheetOptionsDialog() { _dialogState.value = SheetDialogState.SheetOptions }

    fun openShareSheetDialog() { _dialogState.value = SheetDialogState.ShareSheet }

    fun openManageUsersDialog() { _dialogState.value = SheetDialogState.ManageUsers }

    fun openConfirmDeleteSheetDialog() { _dialogState.value = SheetDialogState.ConfirmDeleteSheet }

    fun openConfirmDeleteItemDialog() { _dialogState.value = SheetDialogState.ConfirmDeleteItem }

    fun openConfirmGivePermissionDialog() { _dialogState.value = SheetDialogState.ConfirmGivePermission }

    fun openConfirmRemoveUserDialog() { _dialogState.value = SheetDialogState.ConfirmRemoveUser }

    fun openConfirmLogoutDialog() { _dialogState.value = SheetDialogState.ConfirmLogout }

    fun closeDialog() { _dialogState.value = SheetDialogState.None }

    fun cycleSortType() {
        if (_requestPending.value) return
        _requestPending.value = true
        val sortType = when (_sheet.value.sortType) {
            SheetSortType.MainText -> SheetSortType.Label
            SheetSortType.Label -> SheetSortType.Data
            SheetSortType.Data -> SheetSortType.DateCreated
            SheetSortType.DateCreated -> SheetSortType.DateModified
            SheetSortType.DateModified -> SheetSortType.MainText
        }
        viewModelScope.launch {
            storageService.updateSheet(
                _sheet.value.copy(sortType = sortType)
            )
            reloadSheet()
            _requestPending.value = false
        }
    }

}

enum class SheetDialogState() {
    None,
    AddItem,
    EditItem,
    SheetOptions,
    ShareSheet,
    ManageUsers,
    ConfirmDeleteSheet,
    ConfirmDeleteItem,
    ConfirmGivePermission,
    ConfirmRemoveUser,
    ConfirmLogout,
}