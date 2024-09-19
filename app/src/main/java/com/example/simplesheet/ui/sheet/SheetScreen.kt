package com.example.simplesheet.ui.sheet

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplesheet.R
import com.example.simplesheet.config.StringLength
import com.example.simplesheet.data.SheetFontSize
import com.example.simplesheet.data.SheetSortType
import com.example.simplesheet.data.SheetView
import com.example.simplesheet.ui.components.ConfirmOrCancelDialog
import com.example.simplesheet.ui.components.LoadingOverlay
import com.example.simplesheet.ui.components.LoginButton
import com.example.simplesheet.ui.components.LogoutButton
import com.example.simplesheet.ui.components.TextFieldDialog
import com.example.simplesheet.ui.components.TripleTextFieldDialog
import com.example.simplesheet.ui.dashboard.DashboardDialogState
import com.example.simplesheet.ui.dashboard.DashboardSortType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SheetScreen(
    viewModel: SheetViewModel = hiltViewModel(),
    onBackRequested: () -> Unit,
    onSignedOut: () -> Unit,
) {

    // Perform soft reset of screen data upon reentering this screen
    LaunchedEffect(Unit) {
        viewModel.softReset(onSignedOut)
    }

    val sheet = viewModel.sheet.collectAsState()
    val items = viewModel.items.collectAsState()
    val noteText = viewModel.noteText.collectAsState()
    val editPermission = viewModel.editPermission.collectAsState()
    val requestPending = viewModel.requestPending.collectAsState()
    val dialogState = viewModel.dialogState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var inputText2 by remember { mutableStateOf("") }
    var inputText3 by remember { mutableStateOf("") }
    var inputBoolean by remember { mutableStateOf(false) }
    var inputGmail by remember { mutableStateOf("") }
    var inputView by remember { mutableStateOf(SheetView.PlainList) }
    var inputFontSize by remember { mutableStateOf(SheetFontSize.Small) }
    var inputMaxLines by remember { mutableIntStateOf(1) }

    BackHandler(true) {
        if (!requestPending.value) {
            if (editPermission.value)
                viewModel.updateSheetNoteText(onBackRequested)
            else onBackRequested()
        }
    }

    // UI construction
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = sheet.value.title,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!requestPending.value) {
                                if (editPermission.value)
                                    viewModel.updateSheetNoteText(onBackRequested)
                                else onBackRequested()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {

                    if (viewModel.checkEditPermission()) {

                        IconButton(
                            onClick = {
                                inputText = ""
                                inputText2 = ""
                                inputText3 = ""
                                viewModel.openCreateItemDialog()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add new sheet",
                            )
                        }

                        IconButton(
                            onClick = {
                                when (sheet.value.sortType) {
                                    SheetSortType.DateModified ->
                                        Toast.makeText(
                                            context,
                                            "Sorted alphanumerically",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    SheetSortType.MainText ->
                                        Toast.makeText(
                                            context,
                                            "Sorted by label",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    SheetSortType.Label ->
                                        Toast.makeText(
                                            context,
                                            "Sorted by data",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    SheetSortType.Data ->
                                        Toast.makeText(
                                            context,
                                            "Sorted by date created",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    SheetSortType.DateCreated ->
                                        Toast.makeText(
                                            context,
                                            "Sorted by date modified",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                                viewModel.cycleSortType()
                            },
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.width(24.dp)
                                )
                                when (sheet.value.sortType) {
                                    SheetSortType.MainText ->
                                        Text(
                                            text = "A",
                                            fontSize = 12.sp,
                                            modifier = Modifier.width(12.dp)
                                        )

                                    SheetSortType.Label ->
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                            modifier = Modifier.width(12.dp)
                                        )

                                    SheetSortType.Data ->
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.width(12.dp)
                                        )

                                    SheetSortType.DateCreated ->
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.width(12.dp)
                                        )

                                    SheetSortType.DateModified ->
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.width(12.dp)
                                        )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                inputGmail = ""
                                inputBoolean = false
                                viewModel.openShareSheetDialog()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Share",
                            )
                        }

                        IconButton(
                            onClick = {
                                inputGmail = ""
                                inputBoolean = false
                                viewModel.openManageUsersDialog()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Manage users",
                            )
                        }

                        IconButton(
                            onClick = {
                                inputText = sheet.value.title
                                inputView = sheet.value.view
                                inputFontSize = sheet.value.fontSize
                                inputMaxLines = sheet.value.maxLines
                                viewModel.openSheetOptionsDialog()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Edit current sheet",
                            )
                        }

                    } else {
                        Text(
                            text = "View only mode",
                            fontSize = 16.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )

                        IconButton(
                            onClick = { viewModel.deleteSheet(onBackRequested) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Leave shared sheet",
                            )
                        }
                    }

                    LogoutButton(
                        onSignOut = { viewModel.openConfirmLogoutDialog() },
                        enabled = !requestPending.value,
                    )
                },
            )
        },
        bottomBar = {
            if (sheet.value.view == SheetView.Notepad && editPermission.value) {
                BottomAppBar(
                    actions = {
                        IconButton(
                            onClick = { viewModel.undoNoteChanges() },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redoNoteChanges() },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo"
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.updateSheetNoteText() },
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text("Save")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (sheet.value.view == SheetView.Notepad) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                TextField(
                    value = noteText.value,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    onValueChange = { viewModel.rememberAndUpdateNoteText(it) },
                    readOnly = !editPermission.value,
                    textStyle = LocalTextStyle.current.copy(fontSize = sheet.value.fontSize.value.sp),
                    maxLines = StringLength.NOTEPAD_LINES,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            } else {

                // List of items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }

                    items(items.value.size) { i ->

                        val itemCard = @Composable { content: @Composable () -> Unit ->
                            Card(
                                colors = CardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        onClick = {
                                            val item = viewModel.selectItem(i)
                                            inputText = item.text
                                            inputText2 = item.label
                                            inputText3 = item.data
                                            viewModel.openEditItemDialog()
                                        },
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                                ) {
                                    content()
                                }
                            }
                        }

                        val itemText = @Composable { text: String, color: Color, modifier: Modifier ->
                            Box(
                                modifier = modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = text,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = sheet.value.fontSize.value.sp,
                                    color = color,
                                    maxLines = sheet.value.maxLines,
                                    modifier = modifier.padding(4.dp).fillMaxHeight().wrapContentHeight()
                                )
                            }
                        }

                        val MAIN_TEXT_WEIGHT = 1f
                        val LABEL_WEIGHT = 0.3f
                        val DATA_WEIGHT = 0.5f

                        when (sheet.value.view) {

                            SheetView.PlainList ->
                                itemCard {
                                    itemText(
                                        items.value[i].text,
                                        MaterialTheme.colorScheme.onSurface,
                                        Modifier
                                    )
                                }

                            SheetView.LabeledList ->
                                itemCard {
                                    itemText(
                                        items.value[i].label,
                                        MaterialTheme.colorScheme.onPrimary,
                                        Modifier
                                            .weight(LABEL_WEIGHT)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    itemText(
                                        items.value[i].text,
                                        MaterialTheme.colorScheme.onSurface,
                                        Modifier.weight(MAIN_TEXT_WEIGHT)
                                    )
                                }

                            SheetView.DataList ->
                                itemCard {
                                    itemText(
                                        items.value[i].text,
                                        MaterialTheme.colorScheme.onSurface,
                                        Modifier.weight(MAIN_TEXT_WEIGHT)
                                    )
                                    itemText(
                                        items.value[i].data,
                                        MaterialTheme.colorScheme.primary,
                                        Modifier.weight(DATA_WEIGHT)
                                    )
                                }

                            SheetView.LabeledDataList ->
                                itemCard {
                                    itemText(
                                        items.value[i].label,
                                        MaterialTheme.colorScheme.onPrimary,
                                        Modifier
                                            .weight(LABEL_WEIGHT)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    itemText(
                                        items.value[i].text,
                                        MaterialTheme.colorScheme.onSurface,
                                        Modifier.weight(MAIN_TEXT_WEIGHT)
                                    )
                                    itemText(
                                        items.value[i].data,
                                        MaterialTheme.colorScheme.primary,
                                        Modifier.weight(DATA_WEIGHT)
                                    )
                                }

                            else -> {}
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        when (dialogState.value) {
            SheetDialogState.AddItem ->
                TripleTextFieldDialog(
                    titleText = "Add new item",
                    confirmText = "Create",
                    textFieldLabel1 = "Main text",
                    textFieldLabel2 = "Label text",
                    textFieldLabel3 = "Data text",
                    maxInputSize1 = StringLength.MAIN_TEXT,
                    maxInputSize2 = StringLength.LABEL,
                    maxInputSize3 = StringLength.LABEL,
                    maxLines1 = 12,
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.createItem(inputText, inputText2, inputText3) },
                    setUserInput1 = { inputText = it },
                    setUserInput2 = { inputText2 = it },
                    setUserInput3 = { inputText3 = it },
                    userInput1 = inputText,
                    userInput2 = inputText2,
                    userInput3 = inputText3,
                    focusManager = focusManager,
                    requestPending = requestPending.value,
                    )

            SheetDialogState.EditItem ->
                TripleTextFieldDialog(
                    titleText = "Edit item",
                    confirmText = "Save",
                    tertiaryButtonText = "Delete",
                    textFieldLabel1 = "Main text",
                    textFieldLabel2 = "Label text",
                    textFieldLabel3 = "Data text",
                    maxInputSize1= StringLength.MAIN_TEXT,
                    maxInputSize2 = StringLength.LABEL,
                    maxInputSize3 = StringLength.LABEL,
                    maxLines1 = 12,
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.updateItem(inputText, inputText2, inputText3) },
                    setUserInput1 = { inputText = it },
                    setUserInput2 = { inputText2 = it },
                    setUserInput3 = { inputText3 = it },
                    userInput1 = inputText,
                    userInput2 = inputText2,
                    userInput3 = inputText3,
                    focusManager = focusManager,
                    onTertiaryButtonClicked = { viewModel.openConfirmDeleteItemDialog() },
                    requestPending = requestPending.value,
                    )

            SheetDialogState.SheetOptions ->
                SheetOptionsDialog(
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.updateSheet(inputText, inputView, inputFontSize, inputMaxLines) },
                    onDeleteClicked = { viewModel.openConfirmDeleteSheetDialog() },
                    setInputTitle = { inputText = it },
                    setSheetView = { inputView = it },
                    setSheetFontSize = { inputFontSize = it },
                    setMaxLines = { inputMaxLines = it },
                    inputTitle = inputText,
                    inputView = inputView,
                    inputFontSize = inputFontSize,
                    inputMaxLines = inputMaxLines,
                    focusManager = focusManager,
                    requestPending = requestPending.value,
                )

            SheetDialogState.ShareSheet ->
                ShareSheetDialog(
                    shareCode = sheet.value.id,
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { gmail, editPermission ->
                        if (editPermission) {
                            viewModel.openConfirmGivePermissionDialog()
                        } else viewModel.shareSheet(gmail, false)
                    },
                    onCopyClicked = { viewModel.copyStringToClipboard(context, sheet.value.id, "Invitation code") },
                    setUserInput = { inputGmail = it },
                    setUserSwitchInput = { inputBoolean = it },
                    userInput = inputGmail,
                    userSwitchInput = inputBoolean,
                    focusManager = focusManager,
                    requestPending = requestPending.value,
                )

            SheetDialogState.ManageUsers ->
                ManageUsersDialog(
                    onDismissRequest = { viewModel.closeDialog() },
                    setAsViewer = { viewModel.setUserPermission(it, false) },
                    setAsEditor = {
                        inputGmail = it
                        viewModel.openConfirmGivePermissionDialog()
                    },
                    removeUser = {
                        inputGmail = it
                        viewModel.openConfirmRemoveUserDialog()
                    },
                    editableBy = sheet.value.editableBy,
                    viewableBy = sheet.value.viewableBy,
                    userGmail = viewModel.getUserGmail(),
                    requestPending = requestPending.value,
                )

            SheetDialogState.ConfirmDeleteSheet -> {
                viewModel.checkEditPermission()
                ConfirmOrCancelDialog(
                    titleText = if (editPermission.value) "Delete sheet" else "Leave sheet",
                    descriptionText = if (editPermission.value) "This action cannot be undone." else null,
                    confirmText = if (editPermission.value) "Delete" else "Leave",
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = {
                        if (!requestPending.value) {
                            viewModel.deleteSheet {
                                if (editPermission.value)
                                    viewModel.updateSheetNoteText(onBackRequested)
                                onBackRequested()
                            }
                        }
                    },
                    requestPending = requestPending.value,
                )
            }

            SheetDialogState.ConfirmDeleteItem ->
                ConfirmOrCancelDialog(
                    titleText = "Delete item",
                    descriptionText = "This action cannot be undone.",
                    confirmText = "Delete",
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.deleteItem() },
                    requestPending = requestPending.value,
                )

            SheetDialogState.ConfirmGivePermission ->
                ConfirmOrCancelDialog(
                    titleText = "Give $inputGmail edit permission?",
                    descriptionText = "This user will be able to create, modify, and delete items in this sheet, as well as delete the entire sheet or give permissions to other users.",
                    confirmText = "Confirm",
                    onDismissRequest = {
                        viewModel.closeDialog()
                        viewModel.openManageUsersDialog()
                    },
                    onConfirmClicked = { viewModel.setUserPermission(inputGmail, true) },
                    requestPending = requestPending.value,
                )

            SheetDialogState.ConfirmRemoveUser ->
                ConfirmOrCancelDialog(
                    titleText = "Remove $inputGmail?",
                    descriptionText = "This user will no longer be able to access this sheet.",
                    confirmText = "Remove",
                    onDismissRequest = {
                        viewModel.closeDialog()
                        viewModel.openManageUsersDialog()
                    },
                    onConfirmClicked = { viewModel.removeUser(inputGmail) },
                    requestPending = requestPending.value,
                )

            SheetDialogState.ConfirmLogout ->
                ConfirmOrCancelDialog(
                    titleText = "Are you sure you want to sign out?",
                    confirmText = "Sign out",
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = {
                        if (editPermission.value)
                            viewModel.updateSheetNoteText {
                                viewModel.onSignOut(onSignedOut)
                            }
                        else viewModel.onSignOut(onSignedOut)
                    },
                    requestPending = requestPending.value,
                )

            else -> {}
        }
    }

    LoadingOverlay(requestPending.value)
}