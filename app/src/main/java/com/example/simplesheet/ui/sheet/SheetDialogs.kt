package com.example.simplesheet.ui.sheet

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.simplesheet.R
import com.example.simplesheet.config.StringLength
import com.example.simplesheet.data.SheetFontSize
import com.example.simplesheet.data.SheetView

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ShareSheetDialog(
    onDismissRequest: () -> Unit,
    onConfirmClicked: (String, Boolean) -> Unit,
    onCopyClicked: () -> Unit,
    setUserInput: (String) -> Unit,
    setUserSwitchInput: (Boolean) -> Unit,
    userInput: String?,
    userSwitchInput: Boolean,
    shareCode: String,
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
        var errorState by remember { mutableStateOf(0) }
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
                    text = "Share sheet",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.padding(top = mediumPadding, bottom = mediumPadding)
                ) {
                    OutlinedTextField(
                        value = userInput ?: "",
                        onValueChange = { setUserInput(if (it.length <= StringLength.EMAIL) it else it.substring(0..StringLength.EMAIL)) },
                        label = { Text("Gmail address") },
                        enabled = !requestPending,
                        isError = errorState > 0,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                    if (errorState > 0) {
                        val errorText = when (errorState) {
                            1 -> "This field is required."
                            2 -> "Invalid Gmail format."
                            else -> "Unknown error."
                        }
                        Text(
                            text = errorText,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.height(smallPadding))
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = userSwitchInput,
                            onCheckedChange = { setUserSwitchInput(it) },
                            enabled = !requestPending,
                            modifier = Modifier.padding(end = smallPadding),
                        )
                        Text("Allow editing")
                    }
                }
                Text(
                    text = "Code:",
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = shareCode,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = onCopyClicked,
                            onLongClick = onCopyClicked,
                        )
                )
                Text(
                    text = "*Share this code with people you have added.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = largePadding)
                )
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
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (userInput.isNullOrBlank()) {
                                errorState = 1
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(userInput).matches() || !userInput.endsWith("@gmail.com")) {
                                errorState = 2
                            } else {
                                onConfirmClicked(userInput, userSwitchInput)
                            }
                        },
                        enabled = !requestPending,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.size(120.dp, 40.dp)
                    ) { Text("Invite") }
                }
            }
        }
    }
}

@Composable
fun ManageUsersDialog(
    onDismissRequest: () -> Unit,
    setAsViewer: (String) -> Unit,
    setAsEditor: (String) -> Unit,
    removeUser: (String) -> Unit,
    editableBy: List<String>,
    viewableBy: List<String>,
    userGmail: String,
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
        val scrollState = rememberScrollState()

        val viewableByWithoutEditors = viewableBy.toMutableList()
        viewableByWithoutEditors.removeAll(editableBy)

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
                    text = "Manage users",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                LazyColumn(
                    modifier = Modifier
                        .padding(top = largePadding, bottom = largePadding)
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(editableBy.size) { i ->
                        Card(
                            colors = CardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column (
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(color = MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = smallPadding)
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = "Editor" + if (editableBy[i] == userGmail) " (you)" else "",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(end = smallPadding)
                                            .wrapContentHeight(Alignment.CenterVertically)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { setAsViewer(editableBy[i]) },
                                        enabled = !requestPending && userGmail != editableBy[i],
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveModerator,
                                            contentDescription = "Set user as viewer",
                                        )
                                    }
                                    IconButton(
                                        onClick = { removeUser(editableBy[i]) },
                                        enabled = !requestPending && userGmail != editableBy[i],
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonRemove,
                                            contentDescription = "Remove user",
                                        )
                                    }
                                }
                                Text(
                                    text = editableBy[i],
                                    fontSize = 16.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(start = smallPadding, top = smallPadding, bottom = smallPadding)
                                )
                            }
                        }
                    }
                    items(viewableByWithoutEditors.size) { i ->
                        Card(
                            colors = CardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column (
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(color = MaterialTheme.colorScheme.secondary)
                                        .padding(horizontal = smallPadding)
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = "Viewer" + if (viewableByWithoutEditors[i] == userGmail) " (you)" else "",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(end = smallPadding)
                                            .wrapContentHeight(Alignment.CenterVertically)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { setAsEditor(viewableByWithoutEditors[i]) },
                                        enabled = !requestPending && userGmail != viewableByWithoutEditors[i],
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddModerator,
                                            contentDescription = "Set user as editor",
                                        )
                                    }
                                    IconButton(
                                        onClick = { removeUser(viewableByWithoutEditors[i]) },
                                        enabled = !requestPending && userGmail != viewableByWithoutEditors[i],
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonRemove,
                                            contentDescription = "Remove user",
                                        )
                                    }
                                }
                                Text(
                                    text = viewableByWithoutEditors[i],
                                    fontSize = 16.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(start = smallPadding, top = smallPadding, bottom = smallPadding)
                                )
                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismissRequest,
                        enabled = !requestPending,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.size(120.dp, 40.dp)
                    ) { Text("Close") }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SheetOptionsDialog(
    onDismissRequest: () -> Unit,
    onConfirmClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    setInputTitle: (String) -> Unit,
    setSheetView: (SheetView) -> Unit,
    setSheetFontSize: (SheetFontSize) -> Unit,
    inputTitle: String,
    inputView: SheetView,
    inputFontSize: SheetFontSize,
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
        var isError by remember { mutableStateOf(false) }
        var dropdownMenuWidth by remember { mutableIntStateOf(0) }
        var viewDropdownMenuExpanded by remember { mutableStateOf(false) }
        var fontSizeDropdownMenuExpanded by remember { mutableStateOf(false) }
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
                    text = "Sheet options",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.padding(top = mediumPadding)
                ) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { setInputTitle(if (it.length <= StringLength.TITLE) it else it.substring(0..StringLength.TITLE)) },
                        label = { Text("Title text") },
                        enabled = !requestPending,
                        isError = isError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(
                            FocusDirection.Exit) }),
                    )
                    if (isError) {
                        Text(
                            text = "This field is required.",
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
                        .padding(top = mediumPadding)
                ) {
                    Text(
                        text = "Sheet view:",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = smallPadding)
                            .wrapContentHeight()
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .onGloballyPositioned { coordinates ->
                                dropdownMenuWidth = coordinates.size.width
                            }
                            .combinedClickable(
                                onClick = { viewDropdownMenuExpanded = true },
                                enabled = !requestPending,
                            )
                            .wrapContentHeight()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = mediumPadding)
                        ) {
                            Text(
                                text = inputView.displayName
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (viewDropdownMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand"
                            )
                        }

                        DropdownMenu(
                            expanded = viewDropdownMenuExpanded,
                            onDismissRequest = { viewDropdownMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .width(with(LocalDensity.current) { dropdownMenuWidth.toDp() })
                                .heightIn(0.dp, 240.dp)
                        ) {
                            for (view in SheetView.entries) {
                                DropdownMenuItem(
                                    text = { Text(text = view.displayName) },
                                    onClick = { setSheetView(view); viewDropdownMenuExpanded = false },
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = mediumPadding, bottom = largePadding)
                ) {
                    Text(
                        text = "Sheet font size: ",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = smallPadding)
                            .wrapContentHeight()
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .onGloballyPositioned { coordinates ->
                                dropdownMenuWidth = coordinates.size.width
                            }
                            .combinedClickable(
                                onClick = { fontSizeDropdownMenuExpanded = true },
                                enabled = !requestPending,
                            )
                            .wrapContentHeight()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = mediumPadding)
                        ) {
                            Text(
                                text = inputFontSize.displayName
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (fontSizeDropdownMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand"
                            )
                        }

                        DropdownMenu(
                            expanded = fontSizeDropdownMenuExpanded,
                            onDismissRequest = { fontSizeDropdownMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .width(with(LocalDensity.current) { dropdownMenuWidth.toDp() })
                                .heightIn(0.dp, 240.dp)
                        ) {
                            for (fontSize in SheetFontSize.entries) {
                                DropdownMenuItem(
                                    text = { Text(text = fontSize.displayName) },
                                    onClick = { setSheetFontSize(fontSize); fontSizeDropdownMenuExpanded = false },
                                )
                            }
                        }
                    }
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
                        modifier = Modifier.size(90.dp, 40.dp)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (inputTitle.isBlank()) {
                                isError = true
                            } else {
                                onConfirmClicked()
                            }
                        },
                        enabled = !requestPending,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.size(90.dp, 40.dp)
                    ) {
                        if (requestPending) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .wrapContentHeight()
                            )
                        } else {
                            Text("Save")
                        }
                    }
                    Button(
                        onClick = onDeleteClicked,
                        enabled = !requestPending,
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .size(90.dp, 40.dp)
                    ) {
                        if (requestPending) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(24.dp)
                                    .wrapContentHeight()
                            )
                        } else {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}