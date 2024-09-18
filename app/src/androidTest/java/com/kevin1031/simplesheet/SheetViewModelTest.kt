package com.kevin1031.simplesheet

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.kevin1031.simplesheet.data.AccountService
import com.kevin1031.simplesheet.data.Item
import com.kevin1031.simplesheet.data.Sheet
import com.kevin1031.simplesheet.data.SheetFontSize
import com.kevin1031.simplesheet.data.SheetView
import com.kevin1031.simplesheet.data.StorageService
import com.kevin1031.simplesheet.ui.sheet.SheetDialogState
import com.kevin1031.simplesheet.ui.sheet.SheetViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SheetViewModelTest {
    private lateinit var viewModel: SheetViewModel
    private lateinit var accountService: AccountService
    private lateinit var storageService: StorageService
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        accountService = mockk()
        storageService = mockk()
        savedStateHandle = mockk()
        viewModel = SheetViewModel(accountService, storageService, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun reloadSheet_should_update_sheet_items_and_note_text() = runTest {
        
        val sheet = Sheet(text = "test note")
        val items = listOf(Item(text = "item 1"))
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns items

        viewModel.reloadSheet()
        
        assertEquals(sheet, viewModel.sheet.value)
        assertEquals(items, viewModel.items.value)
        Assert.assertEquals("test note", viewModel.noteText.value)
    }

    @Test
    fun rememberAndUpdateNoteText_should_update_note_text_and_manage_history() = runTest {

        val initialText = "initial text"
        val newText = "new text"
        coEvery { storageService.readSheet(any()) } returns Sheet(text = initialText)
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()

        viewModel.rememberAndUpdateNoteText(newText)

        Assert.assertEquals(newText, viewModel.noteText.value)
        Assert.assertEquals(initialText, viewModel.noteHistory.value.first())
    }

    @Test
    fun rememberAndUpdateNoteText_should_not_exceed_history_size() = runTest {

        val initialText = "0"
        var comparisonText = ""
        coEvery { storageService.readSheet(any()) } returns Sheet(text = initialText)
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()

        for (i in 1..SheetViewModel.HISTORY_SIZE+1) {
            viewModel.rememberAndUpdateNoteText(viewModel.noteText.value + "0")
            comparisonText += "0"
        }

        Log.d("", "$comparisonText ${viewModel.noteText.value}")

        Assert.assertEquals(comparisonText, viewModel.noteHistory.value.first())
        Assert.assertNotEquals(initialText, viewModel.noteHistory.value.last())
        Assert.assertEquals(SheetViewModel.HISTORY_SIZE, viewModel.noteHistory.value.size)
    }

    @Test
    fun undoNoteChanges_should_revert_to_previous_note_text() = runTest {

        val initialText = "initial text"
        val newText = "new text"
        coEvery { storageService.readSheet(any()) } returns Sheet(text = initialText)
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        viewModel.rememberAndUpdateNoteText(newText)

        viewModel.undoNoteChanges()
        
        Assert.assertEquals("initial text", viewModel.noteText.value)
    }

    @Test
    fun redoNoteChanges_should_apply_next_note_text() = runTest {

        val initialText = "initial text"
        val newText = "new text"
        coEvery { storageService.readSheet(any()) } returns Sheet(text = initialText)
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        viewModel.rememberAndUpdateNoteText(newText)
        viewModel.undoNoteChanges()

        viewModel.redoNoteChanges()

        Assert.assertEquals("new text", viewModel.noteText.value)
    }

    @Test
    fun checkEditPermission_should_return_true_if_user_has_permission() = runTest {
        
        val sheet = Sheet(editableBy = listOf("user@gmail.com"))
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        every { accountService.getUserProfile().gmail } returns "user@gmail.com"

        val result = viewModel.checkEditPermission()
        
        Assert.assertTrue(result)
    }

    @Test
    fun getUserGmail_should_return_user_s_gmail() {
        every { accountService.getUserProfile().gmail } returns "user@gmail.com"

        val gmail = viewModel.getUserGmail()
        
        Assert.assertEquals("user@gmail.com", gmail)
    }

    @Test
    fun selectItem_should_return_the_item_at_the_given_index() = runTest {
        
        val item = Item(text = "Test Item")
        coEvery { storageService.readSheet(any()) } returns Sheet()
        coEvery { storageService.readAllItems(any()) } returns listOf(item)
        viewModel.reloadSheet()
        
        val selectedItem = viewModel.selectItem(0)

        
        assertEquals(item, selectedItem)
        assertEquals(item, viewModel.selectedItem.value)
    }

    @Test
    fun createItem_should_create_a_new_item_and_reload_sheet() = runTest {

        val sheet = Sheet()
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        val item = Item(text = "New Item")
        coEvery { storageService.createItem(sheet, item) } returns Unit
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf(item)

        viewModel.createItem("New Item", "", "")
        
        coVerify { storageService.createItem(sheet, item) }
        assertEquals(listOf(item), viewModel.items.value)
        assertEquals(SheetDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun updateItem_should_update_the_item_and_reload_sheet() = runTest {

        val sheet = Sheet()
        val originalItem = Item(text = "Original Item")
        val updatedItem = Item(text = "Updated Item")
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf(originalItem)
        viewModel.reloadSheet()
        coEvery { storageService.updateItem(sheet, updatedItem) } returns Unit
        coEvery { storageService.readAllItems(any()) } returns listOf(updatedItem)
        viewModel.reloadSheet()

        viewModel.updateItem("Updated Item", "", "")

        assertEquals(listOf(updatedItem), viewModel.items.value)
        assertEquals(SheetDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun updateSheet_should_update_the_sheet_and_reload_sheet() = runTest {
        
        val originalSheet = Sheet(title = "Original Title", view = SheetView.PlainList, fontSize = SheetFontSize.Medium)
        val updatedSheet = originalSheet.copy(title = "Updated Title", view = SheetView.Notepad, fontSize = SheetFontSize.Large)
        coEvery { storageService.readSheet(any()) } returns originalSheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        coEvery { storageService.readSheet(any()) } returns updatedSheet
        coEvery { storageService.updateSheet(updatedSheet) } returns Unit
        viewModel.reloadSheet()

        viewModel.updateSheet(title = updatedSheet.title, view = updatedSheet.view, fontSize = updatedSheet.fontSize)

        coVerify { storageService.updateSheet(updatedSheet) }
        assertEquals(updatedSheet, viewModel.sheet.value)
        assertEquals(SheetDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun updateSheetNoteText_should_update_the_sheet_note_and_reload_sheet() = runTest {

        val updatedNoteText = "Updated Note"
        val originalSheet = Sheet(text = "Original Note")
        val updatedSheet = originalSheet.copy(text = updatedNoteText)
        coEvery { storageService.readSheet(any()) } returns originalSheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        viewModel.rememberAndUpdateNoteText(updatedNoteText)
        coEvery { storageService.updateSheet(updatedSheet) } returns Unit
        coEvery { storageService.readSheet(any()) } returns updatedSheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        val doAfter = mockk<() -> Unit>(relaxed = true)

        viewModel.updateSheetNoteText(doAfter)

        coVerify { storageService.updateSheet(updatedSheet) }
        assertEquals(updatedSheet, viewModel.sheet.value)
        assertEquals(SheetDialogState.None, viewModel.dialogState.value)
        verify { doAfter() }
    }

    @Test
    fun delete_item_should_delete_the_item_and_reload_sheet() = runTest {
        val sheet = Sheet()
        val item = Item()
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf(item)
        viewModel.reloadSheet()
        coEvery { storageService.deleteItem(sheet, item) } returns Unit
        coEvery { storageService.readSheet(any()) }returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf()

        viewModel.deleteItem()

        coVerify { storageService.deleteItem(sheet, item) }
        assertTrue(viewModel.items.value.isEmpty())
        assertEquals(SheetDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun delete_sheet_should_delete_the_sheet_reload_and_call_on_back_requested() = runTest {
        // Arrange
        val sheet = Sheet(id = "sheetId")
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        val onBackRequested = mockk<() -> Unit>(relaxed = true)
        coEvery { storageService.deleteSheet("sheetId") } returns Unit
        coEvery { storageService.readSheet(any()) } returns null
        coEvery { storageService.readAllItems(any()) } returns listOf()

        // Act
        viewModel.deleteSheet(onBackRequested)

        // Assert
        coVerify { storageService.deleteSheet("sheetId") }
        assertEquals(Sheet(), viewModel.sheet.value)
        assertEquals(SheetDialogState.None, viewModel.dialogState.value)
        verify { onBackRequested() }
    }

    @Test
    fun share_sheet_should_add_user_to_viewable_by_and_reload_sheet() = runTest {
        val sheet = Sheet(id = "sheetId", viewableBy = mutableListOf())
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        val gmail = "test@gmail.com"
        coEvery { storageService.updateSheet(any()) } returns Unit
        coEvery { storageService.readSheet(any()) } returns sheet.copy(viewableBy = mutableListOf(gmail))
        coEvery { storageService.readAllItems(any()) } returns listOf()

        viewModel.shareSheet(gmail, false)

        coVerify { storageService.updateSheet(sheet.copy(viewableBy = mutableListOf(gmail))) }
        assertEquals(mutableListOf(gmail), viewModel.sheet.value.viewableBy)
        assertEquals(SheetDialogState.ManageUsers, viewModel.dialogState.value)
    }

    @Test
    fun share_sheet_should_add_user_to_viewable_by_and_editable_by_if_edit_permission_is_true() = runTest {
            val sheet = Sheet(id = "sheetId", viewableBy = mutableListOf(), editableBy = mutableListOf())
            coEvery { storageService.readSheet(any()) } returns sheet
            coEvery { storageService.readAllItems(any()) } returns listOf()
            viewModel.reloadSheet()
            val gmail = "test@gmail.com"
            coEvery { storageService.updateSheet(any()) } returns Unit
            coEvery { storageService.readSheet(any()) } returns sheet.copy(viewableBy = mutableListOf(gmail), editableBy = mutableListOf(gmail))
            coEvery { storageService.readAllItems(any()) } returns listOf()

            viewModel.shareSheet(gmail, true)

            coVerify { storageService.updateSheet(sheet.copy(viewableBy = mutableListOf(gmail), editableBy = mutableListOf(gmail))) }
            assertEquals(mutableListOf(gmail), viewModel.sheet.value.viewableBy)
            assertEquals(mutableListOf(gmail), viewModel.sheet.value.editableBy)
            assertEquals(SheetDialogState.ManageUsers, viewModel.dialogState.value)
        }

    @Test
    fun set_user_permission_should_remove_user_from_editable_by_if_edit_permission_is_false() = runTest {
            val sheet = Sheet(id = "sheetId", editableBy = mutableListOf("test@gmail.com"))
            coEvery { storageService.readSheet(any()) } returns sheet
            coEvery { storageService.readAllItems(any()) } returns listOf()
            viewModel.reloadSheet()
            val gmail = "test@gmail.com"
            coEvery { storageService.updateSheet(any()) } returns Unit
            coEvery { storageService.readSheet(any()) } returns sheet.copy(editableBy = mutableListOf())
            coEvery { storageService.readAllItems(any()) } returns listOf()

            viewModel.setUserPermission(gmail, false)

            coVerify { storageService.updateSheet(sheet.copy(editableBy = mutableListOf())) }
            assertTrue(viewModel.sheet.value.editableBy.isEmpty())
            assertEquals(SheetDialogState.ManageUsers, viewModel.dialogState.value)
        }

    @Test
    fun set_user_permission_should_add_user_to_editable_by_if_edit_permission_is_true() = runTest {
            val sheet = Sheet(id = "sheetId", editableBy = mutableListOf())
            coEvery { storageService.readSheet(any()) } returns sheet
            coEvery { storageService.readAllItems(any()) } returns listOf()
            viewModel.reloadSheet()
            val gmail = "test@gmail.com"
            coEvery { storageService.updateSheet(any()) } returns Unit
            coEvery { storageService.readSheet(any()) } returns sheet.copy(editableBy = mutableListOf(gmail))
            coEvery { storageService.readAllItems(any()) } returns listOf()

            viewModel.setUserPermission(gmail, true)

            coVerify { storageService.updateSheet(sheet.copy(editableBy = mutableListOf(gmail))) }
            assertEquals(mutableListOf(gmail), viewModel.sheet.value.editableBy)
            assertEquals(SheetDialogState.ManageUsers, viewModel.dialogState.value)
        }

    @Test
    fun remove_user_should_remove_user_from_viewable_by_and_editable_by() = runTest {
        val sheet = Sheet(id = "sheetId", viewableBy = mutableListOf("test@gmail.com"), editableBy = mutableListOf("test@gmail.com"))
        coEvery { storageService.readSheet(any()) } returns sheet
        coEvery { storageService.readAllItems(any()) } returns listOf()
        viewModel.reloadSheet()
        val gmail = "test@gmail.com"
        coEvery { storageService.updateSheet(any()) } returns Unit
        coEvery { storageService.readSheet(any()) } returns sheet.copy(viewableBy = mutableListOf(), editableBy = mutableListOf())
        coEvery { storageService.readAllItems(any()) } returns listOf()

        viewModel.removeUser(gmail)

        coVerify { storageService.updateSheet(sheet.copy(viewableBy = mutableListOf(), editableBy = mutableListOf())) }
        assertTrue(viewModel.sheet.value.viewableBy.isEmpty())
        assertTrue(viewModel.sheet.value.editableBy.isEmpty())
        assertEquals(SheetDialogState.ManageUsers, viewModel.dialogState.value)
    }

}