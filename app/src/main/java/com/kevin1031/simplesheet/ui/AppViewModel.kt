package com.kevin1031.simplesheet.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin1031.simplesheet.data.AccountService
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class AppViewModel (
    private val accountService: AccountService
): ViewModel() {

    protected val _requestPending = MutableStateFlow(false)
    val requestPending: StateFlow<Boolean> = _requestPending.asStateFlow()

    fun onSignInWithGoogle(credential: Credential, onSignedIn: () -> Unit) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                accountService.signInWithGoogle(googleIdTokenCredential.idToken)
                onSignedIn()
            } else {
                Log.e("E", "Unexpected credential.")
            }
            _requestPending.value = false
        }
    }

    fun checkIfAlreadySignedIn(onSignedIn: () -> Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            onSignedIn()
        }
    }

    fun onSignOut(onSignedOut: () -> Unit) {
        if (_requestPending.value) return
        _requestPending.value = true
        viewModelScope.launch {
            accountService.signOut()
            onSignedOut()
            _requestPending.value = false
        }
    }

    fun copyStringToClipboard(context: Context, string: String, label: String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, string)
        clipboard.setPrimaryClip(clip)
    }

}