package com.kevin1031.simplesheet.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.kevin1031.simplesheet.data.AccountService
import com.kevin1031.simplesheet.data.Sheet
import com.kevin1031.simplesheet.data.StorageService
import com.kevin1031.simplesheet.ui.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountService: AccountService,
    private val storageService: StorageService
): AppViewModel(accountService) {

    private val _sheets = MutableStateFlow(listOf<Sheet>())
    val sheets: StateFlow<List<Sheet?>> = _sheets.asStateFlow()

    private val _selectedSheet = MutableStateFlow(Sheet())
    val selectedSheet: StateFlow<Sheet> = _selectedSheet.asStateFlow()

    private val _dialogState = MutableStateFlow(DashboardDialogState.None)
    val dialogState: StateFlow<DashboardDialogState> = _dialogState.asStateFlow()

    private val _sortType = MutableStateFlow(DashboardSortType.Alphanumerical)
    val sortType: StateFlow<DashboardSortType> = _sortType.asStateFlow()

    fun softReset(onSignedOut: () -> Unit) {
        closeDialog()

        _requestPending.value = true
        viewModelScope.launch {
            reloadAllSheets()
            _requestPending.value = false
        }

        observeAuthenticationState(onSignedOut)
    }

    suspend fun reloadAllSheets() {
        _sheets.value = when (_sortType.value) {
            DashboardSortType.Alphanumerical -> storageService.readAllSheets().sortedBy { it.title }
            DashboardSortType.DateCreated -> storageService.readAllSheets().sortedBy { it.dateCreated }
            DashboardSortType.DateModified -> storageService.readAllSheets().sortedBy { it.dateModified }
        }
    }

    fun observeAuthenticationState(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            accountService.currentUser.collect { user ->
                if (user == null) onSignedOut()
            }
        }
    }

    fun isEditable(): Boolean {
        return _selectedSheet.value.editableBy.contains(accountService.getUserProfile().gmail)
    }

    fun selectSheet(index: Int): Sheet {
        val sheet = _sheets.value.getOrElse(index) { return Sheet() }
        _selectedSheet.value = sheet
        return sheet
    }

    fun createSheet(title: String) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.createSheet(
                Sheet(
                    title = title,
                )
            )
            reloadAllSheets()
            closeDialog()
            _requestPending.value = false
        }
    }

    suspend fun isShareCodeValid(sheetId: String): Boolean {
        val sheet = storageService.readSheet(sheetId)
        return sheet != null && sheet.viewableBy.contains(accountService.getUserProfile().gmail)
    }

    fun joinExistingSheet(sheetId: String, errorDialogEnabled: Boolean) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            if (isShareCodeValid(sheetId)) {
                storageService.joinSheet(sheetId)
                reloadAllSheets()
                closeDialog()
            } else if (errorDialogEnabled) {
                openJoinSheetFailedDialog()
            }
            _requestPending.value = false
        }
    }

    fun updateSheet(title: String) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.updateSheet(
                _selectedSheet.value.copy(title = title)
            )
            reloadAllSheets()
            closeDialog()
            _requestPending.value = false
        }
    }

    fun deleteSheet() {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            storageService.deleteSheet(_selectedSheet.value.id)
            reloadAllSheets()
            closeDialog()
            _requestPending.value = false
        }
    }

    fun openCreateSheetDialog() { _dialogState.value = DashboardDialogState.AddSheet }

    fun openSheetEditDialog() { _dialogState.value = DashboardDialogState.EditSheet }

    fun openJoinSheetFailedDialog() { _dialogState.value = DashboardDialogState.JoinSheetFailed }

    fun openConfirmDeleteSheetDialog() { _dialogState.value = DashboardDialogState.ConfirmDeleteSheet }

    fun openConfirmLogoutDialog() { _dialogState.value = DashboardDialogState.ConfirmLogout }

    fun openConfirmQuitDialog() { _dialogState.value = DashboardDialogState.ConfirmQuit }

    fun closeDialog() { _dialogState.value = DashboardDialogState.None }

    fun cycleSortType() {
        _sortType.value = when (_sortType.value) {
            DashboardSortType.Alphanumerical -> DashboardSortType.DateCreated
            DashboardSortType.DateCreated -> DashboardSortType.DateModified
            DashboardSortType.DateModified -> DashboardSortType.Alphanumerical
        }
        _sheets.value = when (_sortType.value) {
            DashboardSortType.Alphanumerical -> _sheets.value.sortedBy { it.title }
            DashboardSortType.DateCreated -> _sheets.value.sortedBy { it.dateCreated }
            DashboardSortType.DateModified -> _sheets.value.sortedBy { it.dateModified }
        }
    }
}

enum class DashboardDialogState() {
    None,
    AddSheet,
    EditSheet,
    JoinSheetFailed,
    ConfirmDeleteSheet,
    ConfirmLogout,
    ConfirmQuit,
}

enum class DashboardSortType() {
    Alphanumerical,
    DateCreated,
    DateModified,
}