package com.kevin1031.simplesheet

import com.kevin1031.simplesheet.data.AccountService
import com.kevin1031.simplesheet.data.Sheet
import com.kevin1031.simplesheet.data.StorageService
import com.kevin1031.simplesheet.ui.dashboard.DashboardDialogState
import com.kevin1031.simplesheet.ui.dashboard.DashboardSortType
import com.kevin1031.simplesheet.ui.dashboard.DashboardViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var accountService: AccountService
    private lateinit var storageService: StorageService

    @Before
    fun setup() {
        accountService = mockk()
        storageService = mockk()
        viewModel = DashboardViewModel(accountService, storageService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cycleSortType_should_cycle_sortType_correctly() = runTest {
        assertEquals(DashboardSortType.Alphanumerical, viewModel.sortType.value)

        viewModel.cycleSortType()
        assertEquals(DashboardSortType.DateCreated, viewModel.sortType.value)

        viewModel.cycleSortType()
        assertEquals(DashboardSortType.DateModified, viewModel.sortType.value)

        viewModel.cycleSortType()
        assertEquals(DashboardSortType.Alphanumerical, viewModel.sortType.value)
    }

    @Test
    fun reloadAllSheets_should_sort_sheets_correctly() = runTest {

        val sheet1 = Sheet(title = "Bravo", dateCreated = 1, dateModified = 3)
        val sheet2 = Sheet(title = "Alpha", dateCreated = 3, dateModified = 1)
        val sheet3 = Sheet(title = "Charlie", dateCreated = 2, dateModified = 2)

        coEvery { storageService.readAllSheets() } returns listOf(sheet1, sheet2, sheet3)

        // Alphanumerical
        viewModel.reloadAllSheets()
        assertEquals(listOf(sheet2, sheet1, sheet3), viewModel.sheets.value)

        // DateCreated
        viewModel.cycleSortType()
        viewModel.reloadAllSheets()
        assertEquals(listOf(sheet1, sheet3, sheet2), viewModel.sheets.value)

        // DateModified
        viewModel.cycleSortType()
        viewModel.reloadAllSheets()
        assertEquals(listOf(sheet2, sheet3, sheet1), viewModel.sheets.value)
    }

    @Test
    fun observeAuthenticationState_should_call_onSignedOut_when_user_is_null() = runTest {
        
        val onSignedOut = mockk<() -> Unit>(relaxed = true)
        every { accountService.currentUser } returns flowOf(null)

        viewModel.observeAuthenticationState(onSignedOut)
        
        verify { onSignedOut() }
    }

    @Test
    fun isEditable_should_return_true_when_user_is_in_editableBy_list() = runTest {

        val sheet = Sheet(editableBy = listOf("user@gmail.com"))
        coEvery { storageService.readAllSheets() } returns listOf(sheet)
        viewModel.reloadAllSheets()
        viewModel.selectSheet(0)
        every { accountService.getUserProfile().gmail } returns "user@gmail.com"

        val result = viewModel.isEditable()

        assertTrue(result)
    }

    @Test
    fun selectSheet_should_return_the_sheet_at_the_given_index() = runTest {

        val sheet = Sheet(title = "Test Sheet")
        coEvery { storageService.readAllSheets() } returns listOf(sheet)
        viewModel.reloadAllSheets()
        viewModel.selectSheet(0)

        val selectedSheet = viewModel.selectSheet(0)

        assertEquals(sheet, selectedSheet)
        assertEquals(sheet, viewModel.selectedSheet.value)
    }

    @Test
    fun createSheet_should_create_a_new_sheet_and_reload_all_sheets() = runTest {
        
        val title = "New Sheet"
        coEvery { storageService.createSheet(any()) } returns Unit
        coEvery { storageService.readAllSheets()} returns listOf(Sheet(title = title))
        
        viewModel.createSheet(title)

        coVerify { storageService.createSheet(any()) }
        assertEquals(listOf(Sheet(title = title)), viewModel.sheets.value)
        assertEquals(DashboardDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun isShareCodeValid_should_return_true_for_valid_sheetId_and_user_access() = runTest {
        
        val sheetId = "validSheetId"
        val sheet = Sheet(id = sheetId, viewableBy = listOf("user@gmail.com"))
        coEvery { storageService.readSheet(sheetId) } returns sheet
        every { accountService.getUserProfile().gmail } returns "user@gmail.com"

        val result = viewModel.isShareCodeValid(sheetId)

        assertTrue(result)
    }

    @Test
    fun joinExistingSheet_should_join_sheet_and_reload_if_share_code_is_valid() = runTest {

        val sheetId = "validSheetId"
        coEvery { storageService.readSheet(sheetId) } returns Sheet(id = sheetId, viewableBy = listOf("user@gmail.com"))
        every { accountService.getUserProfile().gmail } returns "user@gmail.com"
        coEvery { storageService.joinSheet(sheetId) } returns Unit
        coEvery { storageService.readAllSheets() } returns listOf(Sheet(id = sheetId))

        viewModel.joinExistingSheet(sheetId, false)
        
        coVerify { storageService.joinSheet(sheetId) }
        assertEquals(listOf(Sheet(id = sheetId)), viewModel.sheets.value)
        assertEquals(DashboardDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun updateSheet_should_update_the_sheet_and_reload_all_sheets() =runTest{

        val originalSheet = Sheet(id = "sheetId", title = "Original Title")
        val updatedSheet = originalSheet.copy(title = "Updated Title")
        coEvery { storageService.readAllSheets() } returns listOf(originalSheet)
        viewModel.reloadAllSheets()
        viewModel.selectSheet(0)
        coEvery { storageService.updateSheet(any()) } returns Unit
        coEvery { storageService.readAllSheets() } returns listOf(updatedSheet)
        
        viewModel.updateSheet("Updated Title")
        
        coVerify { storageService.updateSheet(updatedSheet) }
        assertEquals(listOf(updatedSheet), viewModel.sheets.value)
        assertEquals(DashboardDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun deleteSheet_should_delete_the_sheet_and_reload_all_sheets() = runTest {
        
        val sheet = Sheet(id = "sheetId")
        coEvery { storageService.readAllSheets() } returns listOf(sheet)
        viewModel.reloadAllSheets()
        viewModel.selectSheet(0)
        coEvery { storageService.deleteSheet(any()) } returns Unit
        coEvery { storageService.readAllSheets() } returns emptyList()

        viewModel.deleteSheet()

        coVerify { storageService.deleteSheet("sheetId") }
        assertTrue(viewModel.sheets.value.isEmpty())
        assertEquals(DashboardDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun openCreateSheetDialog_should_set_dialogState_to_AddSheet() {
        viewModel.openCreateSheetDialog()
        assertEquals(DashboardDialogState.AddSheet, viewModel.dialogState.value)
    }

    @Test
    fun openSheetEditDialog_should_set_dialogState_to_EditSheet() {
        viewModel.openSheetEditDialog()
        assertEquals(DashboardDialogState.EditSheet, viewModel.dialogState.value)
    }

    @Test
    fun openJoinSheetFailedDialog_should_set_dialogState_to_JoinSheetFailed() {
        viewModel.openJoinSheetFailedDialog()
        assertEquals(DashboardDialogState.JoinSheetFailed, viewModel.dialogState.value)
    }

    @Test
    fun openConfirmDeleteSheetDialog_should_set_dialogState_to_ConfirmDeleteSheet() {
        viewModel.openConfirmDeleteSheetDialog()
        assertEquals(DashboardDialogState.ConfirmDeleteSheet, viewModel.dialogState.value)
    }

    @Test
    fun openConfirmLogoutDialog_should_set_dialogState_to_ConfirmLogout() {
        viewModel.openConfirmLogoutDialog()
        assertEquals(DashboardDialogState.ConfirmLogout, viewModel.dialogState.value)
    }

    @Test
    fun closeDialog_should_set_dialogState_to_None() {
        viewModel.closeDialog()
        assertEquals(DashboardDialogState.None, viewModel.dialogState.value)
    }
}