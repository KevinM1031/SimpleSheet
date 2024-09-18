package com.kevin1031.simplesheet.ui.AccountManagement

import com.kevin1031.simplesheet.data.AccountService
import com.kevin1031.simplesheet.ui.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val accountService: AccountService
): AppViewModel(accountService) {

    private val _dialogState = MutableStateFlow(AccountManagementDialogState.None)
    val dialogState: StateFlow<AccountManagementDialogState> = _dialogState.asStateFlow()

    fun openConfirmQuitDialog() { _dialogState.value = AccountManagementDialogState.ConfirmQuit }

    fun closeDialog() { _dialogState.value = AccountManagementDialogState.None }
}

enum class AccountManagementDialogState() {
    None,
    ConfirmQuit,
}