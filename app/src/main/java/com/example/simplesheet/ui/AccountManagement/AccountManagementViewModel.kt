package com.example.simplesheet.ui.AccountManagement

import com.example.simplesheet.data.AccountService
import com.example.simplesheet.ui.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val accountService: AccountService
): AppViewModel(accountService) {


}