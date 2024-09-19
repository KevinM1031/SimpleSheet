package com.example.simplesheet.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.simplesheet.R
import com.example.simplesheet.config.StringLength

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddSheetDialog(
    onDismissRequest: () -> Unit,
    onCreateSheet: (String) -> Unit,
    onJoinSheet: (String, Boolean) -> Unit,
    setGmailInput: (String) -> Unit,
    setShareCodeInput: (String) -> Unit,
    gmailInput: String?,
    shareCodeInput: String?,
    focusManager: FocusManager,
    requestPending: Boolean,
) {
    Box(modifier = Modifier
        .fillMaxSize()
    ) {}
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = !requestPending,
            dismissOnClickOutside = !requestPending,
        ),
    ) {

        val smallPadding = dimensionResource(R.dimen.padding_small)
        val mediumPadding = dimensionResource(R.dimen.padding_medium)
        val largePadding = dimensionResource(R.dimen.padding_large)
        var gmailError by remember { mutableStateOf(false) }
        var shareCodeError by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(mediumPadding)
                    .fillMaxWidth()

            ) {
                Text(
                    text = "Add new sheet",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.padding(top = mediumPadding, bottom = largePadding)
                ) {
                    OutlinedTextField(
                        value = gmailInput ?: "",
                        onValueChange = { setGmailInput(if (it.length <= StringLength.EMAIL) it else it.substring(0..StringLength.EMAIL)) },
                        label = { Text("Sheet name") },
                        isError = gmailError,
                        enabled = shareCodeInput.isNullOrBlank() && !requestPending,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                    if (gmailError && shareCodeInput.isNullOrBlank()) {
                        Text(
                            text = "This field is required",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = "or",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                    if (shareCodeError) {
                        Text(
                            text = "This code is invalid.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    OutlinedTextField(
                        value = shareCodeInput ?: "",
                        onValueChange = { setShareCodeInput(if (it.length <= StringLength.CODE) it else it.substring(0..StringLength.CODE)) },
                        label = { Text("Invitation code") },
                        isError = shareCodeError,
                        enabled = gmailInput.isNullOrBlank() && !requestPending,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        enabled = !requestPending,
                        modifier = Modifier.size(120.dp, 40.dp),
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (gmailInput.isNullOrBlank() && shareCodeInput.isNullOrBlank()) {
                                gmailError = true
                            } else if (shareCodeInput.isNullOrBlank()) {
                                onCreateSheet(gmailInput!!)
                            } else {
                                onJoinSheet(shareCodeInput, true)
                            }
                        },
                        enabled = !requestPending,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.size(120.dp, 40.dp)
                    ) {
                        if (requestPending) {
                            CircularProgressIndicator(modifier = Modifier
                                .width(24.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        } else {
                            Text(if (shareCodeInput.isNullOrBlank()) "Create" else "Join")
                        }
                    }
                }
            }
        }
    }
}