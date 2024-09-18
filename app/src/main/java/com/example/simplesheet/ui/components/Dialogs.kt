package com.example.simplesheet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.simplesheet.R
import com.example.simplesheet.config.StringLength

@Composable
fun AlertDialog(
    text: String,
    closeText: String = "Close",
    descriptionText: String? = null,
    onDismissRequest: () -> Unit,
) {
    val smallPadding = dimensionResource(R.dimen.padding_medium)
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val largePadding = dimensionResource(R.dimen.padding_large)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier
        .fillMaxSize())
    Dialog(onDismissRequest = { onDismissRequest() }) {

        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(mediumPadding)
                    .fillMaxWidth()

            ) {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                if (descriptionText != null) {
                    Text(
                        text = descriptionText,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = mediumPadding, bottom = largePadding)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = largePadding)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.size(120.dp, 40.dp)
                    ) { Text(closeText) }
                }
            }
        }
    }
}

@Composable
fun TextDialog(
    text: String,
    closeText: String = "Close",
    onDismissRequest: () -> Unit,
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val largePadding = dimensionResource(R.dimen.padding_large)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier
        .fillMaxSize())
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(mediumPadding)
                    .fillMaxWidth()
            ) {
                Text(text = text, textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.height(largePadding))
                Button(
                    onClick = { onDismissRequest() },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.size(120.dp, 40.dp)
                ) {
                    Text(text = closeText)
                }
            }
        }
    }
}

@Composable
fun ConfirmOrCancelDialog(
    titleText: String,
    descriptionText: String? = null,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onDismissRequest: () -> Unit,
    onConfirmClicked: () -> Unit,
    requestPending: Boolean,
) {
    val smallPadding = dimensionResource(R.dimen.padding_small)
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val largePadding = dimensionResource(R.dimen.padding_large)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier
        .fillMaxSize())
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = !requestPending,
            dismissOnClickOutside = !requestPending,
        ),
    ) {

        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(mediumPadding)
                    .fillMaxWidth()

            ) {
                Text(
                    text = titleText,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier =
                    if (descriptionText == null)
                        Modifier.padding(top = mediumPadding, bottom = largePadding)
                    else Modifier
                )
                if (descriptionText != null) {
                    Text(
                        text = descriptionText,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = mediumPadding, bottom = largePadding)
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
                        modifier = Modifier.size(120.dp, 40.dp)
                    ) { Text(cancelText) }
                    Button(
                        onClick = onConfirmClicked,
                        shape = RoundedCornerShape(0.dp),
                        enabled = !requestPending,
                        modifier = Modifier.size(120.dp, 40.dp)
                    ) {
                        if (requestPending) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        } else {
                            Text(confirmText)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextFieldDialog(
    titleText: String,
    textFieldLabel: String,
    confirmText: String = "Save",
    cancelText: String = "Cancel",
    tertiaryButtonText: String? = null,
    errorText: String = "This field is required.",
    maxInputSize: Int,
    onDismissRequest: () -> Unit,
    onConfirmClicked: (String) -> Unit,
    onTertiaryButtonClicked: () -> Unit = {},
    setUserInput: (String) -> Unit,
    userInput: String?,
    focusManager: FocusManager,
    requestPending: Boolean,
) {
    Box(modifier = Modifier
        .fillMaxSize()) {}
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
        var isError by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth().verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(mediumPadding)
                    .fillMaxWidth()

            ) {
                Text(
                    text = titleText,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.padding(top = mediumPadding, bottom = largePadding)
                ) {
                    OutlinedTextField(
                        value = userInput ?: "",
                        onValueChange = { setUserInput(if (it.length <= maxInputSize) it else it.substring(0..maxInputSize)) },
                        label = { Text(textFieldLabel) },
                        enabled = !requestPending,
                        isError = isError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                    if (isError) {
                        Text(
                            text = errorText,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val buttonWidth = if (tertiaryButtonText != null) 90.dp else 120.dp
                    TextButton(
                        onClick = onDismissRequest,
                        enabled = !requestPending,
                        modifier = Modifier.size(buttonWidth, 40.dp)
                    ) { Text(cancelText) }
                    Button(
                        onClick = {
                            if (userInput.isNullOrBlank()) {
                                isError = true
                            } else {
                                onConfirmClicked(userInput)
                            }
                        },
                        shape = RoundedCornerShape(0.dp),
                        enabled = !requestPending,
                        modifier = Modifier.size(buttonWidth, 40.dp)
                    ) {
                        if (requestPending) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        } else {
                            Text(confirmText)
                        }
                    }
                    if (tertiaryButtonText != null) {
                        Button(
                            onClick = onTertiaryButtonClicked,
                            enabled = !requestPending,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .size(buttonWidth, 40.dp)
                        ) {
                            if (requestPending) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            } else {
                                Text(tertiaryButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TripleTextFieldDialog(
    titleText: String,
    textFieldLabel1: String,
    textFieldLabel2: String,
    textFieldLabel3: String,
    confirmText: String = "Save",
    cancelText: String = "Cancel",
    tertiaryButtonText: String? = null,
    errorText: String = "This field is required.",
    maxInputSize1: Int,
    maxInputSize2: Int,
    maxInputSize3: Int,
    maxLines1: Int = 1,
    maxLines2: Int = 1,
    maxLines3: Int = 1,
    onDismissRequest: () -> Unit,
    onConfirmClicked: () -> Unit,
    onTertiaryButtonClicked: () -> Unit = {},
    setUserInput1: (String) -> Unit,
    setUserInput2: (String) -> Unit,
    setUserInput3: (String) -> Unit,
    userInput1: String?,
    userInput2: String?,
    userInput3: String?,
    focusManager: FocusManager,
    requestPending: Boolean,
) {
    Box(modifier = Modifier
        .fillMaxSize())
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
        var isError by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        Card(
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth().verticalScroll(scrollState)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(mediumPadding)
                    .fillMaxWidth()

            ) {
                Text(
                    text = titleText,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.padding(top = mediumPadding)
                ) {
                    OutlinedTextField(
                        value = userInput1 ?: "",
                        onValueChange = { setUserInput1(if (it.length <= maxInputSize1) it else it.substring(0..maxInputSize1)) },
                        label = { Text(textFieldLabel1) },
                        enabled = !requestPending,
                        isError = isError,
                        maxLines = maxLines1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                    if (isError) {
                        Text(
                            text = errorText,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(top = mediumPadding)
                ) {
                    OutlinedTextField(
                        value = userInput2 ?: "",
                        onValueChange = { setUserInput2(if (it.length <= maxInputSize2) it else it.substring(0..maxInputSize2)) },
                        label = { Text(textFieldLabel2) },
                        enabled = !requestPending,
                        maxLines = maxLines2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                }
                Column(
                    modifier = Modifier.padding(top = mediumPadding, bottom = largePadding)
                ) {
                    OutlinedTextField(
                        value = userInput3 ?: "",
                        onValueChange = { setUserInput3(if (it.length <= maxInputSize3) it else it.substring(0..maxInputSize3)) },
                        label = { Text(textFieldLabel3) },
                        enabled = !requestPending,
                        maxLines = maxLines3,
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
                    val buttonWidth = if (tertiaryButtonText != null) 90.dp else 120.dp
                    TextButton(
                        onClick = onDismissRequest,
                        enabled = !requestPending,
                        modifier = Modifier.size(buttonWidth, 40.dp)
                    ) { Text(cancelText) }
                    Button(
                        onClick = {
                            if (userInput1.isNullOrBlank()) {
                                isError = true
                            } else {
                                onConfirmClicked()
                            }
                        },
                        shape = RoundedCornerShape(0.dp),
                        enabled = !requestPending,
                        modifier = Modifier.size(buttonWidth, 40.dp)
                    ) {
                        if (requestPending) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )
                        } else {
                            Text(confirmText)
                        }
                    }
                    if (tertiaryButtonText != null) {
                        Button(
                            onClick = onTertiaryButtonClicked,
                            enabled = !requestPending,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .size(buttonWidth, 40.dp)
                        ) {
                            if (requestPending) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            } else {
                                Text(tertiaryButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}