package com.kevin1031.simplesheet.ui.AccountManagement

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.kevin1031.simplesheet.R
import com.kevin1031.simplesheet.ui.components.ConfirmOrCancelDialog
import com.kevin1031.simplesheet.ui.components.LoadingOverlay
import com.kevin1031.simplesheet.ui.components.LoginButton

@Composable
fun AccountManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountManagementViewModel = hiltViewModel(),
    onSignedIn: () -> Unit,
) {

    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    val coroutineScope = rememberCoroutineScope()
    val requestPending = viewModel.requestPending.collectAsState()
    val dialogState = viewModel.dialogState.collectAsState()

    var credentialChecked by remember { mutableStateOf(false) }

    LaunchedEffect(null) {
        viewModel.checkIfAlreadySignedIn(onSignedIn)
        credentialChecked = true
    }

    BackHandler {
        viewModel.openConfirmQuitDialog()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .size(256.dp)
        )
        Text(
            text = "SimpleSheet",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 96.dp)
        )

        LoginButton(
            context = context,
            credentialManager = credentialManager,
            coroutineScope = coroutineScope,
            onSignInWithGoogle = { viewModel.onSignInWithGoogle(it, onSignedIn) },
            enabled = credentialChecked,
        )
    }

    when (dialogState.value) {
        AccountManagementDialogState.ConfirmQuit ->
            ConfirmOrCancelDialog(
                titleText = "Quit app?",
                onDismissRequest = { viewModel.closeDialog() },
                onConfirmClicked = {
                    val activity = (context as? Activity)
                    activity?.finish()
                },
                requestPending = requestPending.value,
            )

        else -> LoadingOverlay(requestPending.value || !credentialChecked)
    }
}
