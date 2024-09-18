import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
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
import com.example.simplesheet.ui.components.AlertDialog
import com.example.simplesheet.ui.components.ConfirmOrCancelDialog
import com.example.simplesheet.ui.components.LoadingOverlay
import com.example.simplesheet.ui.components.LoginButton
import com.example.simplesheet.ui.components.LogoutButton
import com.example.simplesheet.ui.components.TextFieldDialog
import com.example.simplesheet.ui.dashboard.AddSheetDialog
import com.example.simplesheet.ui.dashboard.DashboardDialogState
import com.example.simplesheet.ui.dashboard.DashboardSortType
import com.example.simplesheet.ui.dashboard.DashboardViewModel
import com.example.simplesheet.ui.sheet.SheetDialogState
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSheetClicked: (String) -> Unit,
    onSignedOut: () -> Unit,
    ) {


    // Perform soft reset of screen data upon reentering this screen
    LaunchedEffect(Unit) {
        viewModel.softReset(onSignedOut)
    }

    val sheets = viewModel.sheets.collectAsState()
    val requestPending = viewModel.requestPending.collectAsState()
    val dialogState = viewModel.dialogState.collectAsState()
    val sortType = viewModel.sortType.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var inputText1 by remember { mutableStateOf("") }
    var inputText2 by remember { mutableStateOf("") }

    // UI construction
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All sheets",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            when (sortType.value) {
                                DashboardSortType.DateModified ->
                                    Toast.makeText(context, "Sorted alphanumerically", Toast.LENGTH_SHORT).show()
                                DashboardSortType.Alphanumerical ->
                                    Toast.makeText(context, "Sorted by date created", Toast.LENGTH_SHORT).show()
                                DashboardSortType.DateCreated ->
                                    Toast.makeText(context, "Sorted by date modified", Toast.LENGTH_SHORT).show()
                            }
                            viewModel.cycleSortType()
                        },
                        enabled = !requestPending.value,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                modifier = Modifier.width(24.dp)
                            )
                            when (sortType.value) {
                                DashboardSortType.Alphanumerical ->
                                    Text(
                                        text = "A",
                                        fontSize = 12.sp,
                                        modifier = Modifier.width(12.dp)
                                    )
                                DashboardSortType.DateCreated ->
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.width(12.dp)
                                    )
                                DashboardSortType.DateModified ->
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
                            viewModel.openCreateSheetDialog()
                            inputText1 = ""
                            inputText2 = ""
                        },
                        enabled = !requestPending.value,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create new sheet",
                        )
                    }

                    LogoutButton(
                        onSignOut = { viewModel.openConfirmLogoutDialog() },
                        enabled = !requestPending.value,
                    )
                },
            )
        }
    ) { innerPadding ->

        // List of sheets
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }

            items(sheets.value.size) { i ->

                // Sheet display UI
                Card(
                    shape = RectangleShape,
                    colors = CardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    onClick = {
                        onSheetClicked(sheets.value[i]!!.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = sheets.value[i]!!.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                inputText1 = viewModel.selectSheet(i).title
                                viewModel.openSheetEditDialog()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                            )
                        }
                    }
                }
            }
        }


        when (dialogState.value) {
            DashboardDialogState.AddSheet ->
                AddSheetDialog(
                    onDismissRequest = { viewModel.closeDialog(); },
                    onCreateSheet = { viewModel.createSheet(inputText1) },
                    onJoinSheet = { code, errorDialogEnabled -> viewModel.joinExistingSheet(code, errorDialogEnabled) },
                    setGmailInput = { inputText1 = it },
                    setShareCodeInput = { inputText2 = it },
                    gmailInput = inputText1,
                    shareCodeInput = inputText2,
                    focusManager = focusManager,
                    requestPending = requestPending.value,
                )

            DashboardDialogState.EditSheet ->
                TextFieldDialog(
                    titleText = "Edit sheet",
                    confirmText = "Save",
                    tertiaryButtonText = "Delete",
                    textFieldLabel = "Sheet name",
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.updateSheet(inputText1) },
                    onTertiaryButtonClicked = { viewModel.openConfirmDeleteSheetDialog() },
                    maxInputSize = StringLength.TITLE,
                    setUserInput = { inputText1 = it },
                    userInput = inputText1,
                    focusManager = focusManager,
                    requestPending = requestPending.value,
                )

            DashboardDialogState.JoinSheetFailed ->
                AlertDialog(
                    text = "Invalid invitation code",
                    descriptionText = "Invitation code either does not exist or you do not have the permission to use it.",
                    closeText = "Close",
                    onDismissRequest = { viewModel.closeDialog() },
                )

            DashboardDialogState.ConfirmDeleteSheet -> {
                val isEditable = viewModel.isEditable()
                ConfirmOrCancelDialog(
                    titleText = if (isEditable) "Delete sheet" else "Leave sheet",
                    descriptionText = "This action cannot be undone.",
                    confirmText = if (isEditable) "Delete" else "Leave",
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.deleteSheet() },
                    requestPending = requestPending.value,
                )
            }

            DashboardDialogState.ConfirmLogout ->
                ConfirmOrCancelDialog(
                    titleText = "Are you sure you want to sign out?",
                    confirmText = "Sign out",
                    onDismissRequest = { viewModel.closeDialog() },
                    onConfirmClicked = { viewModel.onSignOut(onSignedOut) },
                    requestPending = requestPending.value,
                )

            else -> LoadingOverlay(requestPending.value)
        }
    }
}