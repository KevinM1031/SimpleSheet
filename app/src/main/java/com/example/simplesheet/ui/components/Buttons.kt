package com.example.simplesheet.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginButton(
    context: Context,
    coroutineScope: CoroutineScope,
    onSignInWithGoogle: (Credential) -> Unit,
    credentialManager: CredentialManager,
    modifier: Modifier = Modifier,
    enabled: Boolean,
) {

    Button(
        onClick = {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("940821727466-bt07157i4jvip5a3bbhb92gmen9bfafr.apps.googleusercontent.com") // this is from app/build/generated/res/processDebugGoogleServices/values/values.xml (default_web_client_id)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            coroutineScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )

                    onSignInWithGoogle(result.credential)
                } catch (e: GetCredentialException) {
                    Log.d("E", e.message.orEmpty())
                }
            }
        },
        shape = RoundedCornerShape(0.dp),
        enabled = enabled,
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier,
    ) {
        Text(
            text = "Sign in with Google",
            fontSize = 16.sp,
            modifier = modifier.padding(0.dp, 6.dp)
        )
    }
}

@Composable
fun LogoutButton(
    onSignOut: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {

    IconButton(
        onClick = {
            onSignOut()
        },
        enabled = enabled,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = "Logout",
        )
    }
}