package com.example.simplesheet.data

import android.text.Editable
import android.util.Log
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.List

class StorageServiceImpl @Inject constructor(private val auth: AccountService) : StorageService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val sheets: Flow<List<Sheet>>
        get() =
            auth.currentUser.flatMapLatest { sheet ->
                Firebase.firestore
                    .collection(SHEETS_COLLECTION)
                    .dataObjects()
            }

    override suspend fun createSheet(sheet: Sheet) {

        // Create sheet
        val gmail = Firebase.auth.currentUser.toAppUser().gmail
        val newSheet = sheet.copy(
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis(),
            viewableBy = listOf(gmail),
            editableBy = listOf(gmail),
        )

        val sheetRef = try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .add(newSheet).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to create sheet.", e)
            return
        }

        // Add sheet ID to user data
        try {
            Firebase.firestore
                .collection(USERDATA_COLLECTION)
                .document(auth.currentUserId)
                .update("sheets", FieldValue.arrayUnion(sheetRef.id)).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update user data.", e)
        }
    }

    override suspend fun joinSheet(sheetId: String) {

        // Add sheet ID to user's "sheets"
        try {
            Firebase.firestore
                .collection(USERDATA_COLLECTION)
                .document(auth.currentUserId)
                .update("sheets", FieldValue.arrayUnion(sheetId)).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update user data.", e)
        }
    }

    override suspend fun createItem(sheet: Sheet, item: Item) {

        val newSheet = sheet.copy( dateModified = System.currentTimeMillis() )

        try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id).set(newSheet).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update sheet data.", e)
        }

        val newItem = item.copy(
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis(),
        )

        try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id)
                .collection(ITEMS_COLLECTION)
                .add(newItem).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to create item.", e)
        }
    }

    override suspend fun readSheet(sheetId: String): Sheet? {

        // Read sheet
        return try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheetId)
                .get().await().toObject()

        } catch (e: FirebaseFirestoreException) {
            // If read fails due to permission denial, forget this sheet ID
            Log.e("StorageServiceImpl", "Permission denied to read sheet data.", e)
            forgetSheet(sheetId)
            null

        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Error occurred when trying to read sheet data.", e)
            null
        }
    }

    override suspend fun readAllSheets(): List<Sheet> {
        return try {

            // Get current user data
            val userData = Firebase.firestore
                .collection(USERDATA_COLLECTION)
                .document(auth.currentUserId)
                .get().await().toObject<UserData>()

            // Read all viewable sheets
            val sheets = mutableListOf<Sheet>()

            userData?.sheets?.forEach {
                val sheet = readSheet(it)
                if (sheet != null)
                    sheets.add(sheet)

            } ?: return emptyList()

            return sheets

        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun readAllItems(sheetId: String): List<Item> {

        return try {
            Firebase.firestore
            .collection(SHEETS_COLLECTION)
            .document(sheetId)
            .collection(ITEMS_COLLECTION)
            .get().await().documents.map {
                it.toObject() ?: Item()
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateSheet(sheet: Sheet) {

        // Update sheet and its update date
        try {
            val newSheet = sheet.copy(dateModified = System.currentTimeMillis())
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id).set(newSheet).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update sheet data.", e)
        }
    }

    override suspend fun updateItem(sheet: Sheet, item: Item) {

        // Update sheet's modified date
        try {
            val newSheet = sheet.copy(dateModified = System.currentTimeMillis())
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id).set(newSheet).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update sheet data.", e)
        }

        // Update item and its modified date
        try {
            val newItem = item.copy(dateModified = System.currentTimeMillis())
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id)
                .collection(ITEMS_COLLECTION)
                .document(item.id).set(newItem).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update item.", e)
        }
    }

    override suspend fun deleteSheet(sheetId: String) {

        // Delete sheet
        try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheetId).delete().await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to delete sheet.", e)
        }

        forgetSheet(sheetId)
    }

    override suspend fun forgetSheet(sheetId: String) {

        // Delete sheet ID from user's "sheets"
        try {
            Firebase.firestore
                .collection(USERDATA_COLLECTION)
                .document(auth.currentUserId)
                .update("sheets", FieldValue.arrayRemove(sheetId)).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update user data.", e)
        }
    }

    override suspend fun deleteItem(sheet: Sheet, item: Item) {

        // Update sheet's modified date
        val newSheet = sheet.copy( dateModified = System.currentTimeMillis() )
        try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id).set(newSheet).await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to update sheet data.", e)
        }

        // Delete item
        try {
            Firebase.firestore
                .collection(SHEETS_COLLECTION)
                .document(sheet.id)
                .collection(ITEMS_COLLECTION)
                .document(item.id).delete().await()
        } catch (e: Exception) {
            Log.e("StorageServiceImpl", "Permission denied to delete item.", e)
        }
    }

    companion object {
        private const val USERDATA_COLLECTION = "users"

        private const val SHEETS_COLLECTION = "sheets"
        private const val ITEMS_COLLECTION = "items"
    }

    private fun FirebaseUser?.toAppUser(): User {
        return if (this == null) User() else User(
            uid = this.uid,
            gmail = this.email ?: "",
        )
    }
}