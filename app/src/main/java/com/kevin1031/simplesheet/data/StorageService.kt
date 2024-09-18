package com.kevin1031.simplesheet.data

import kotlinx.coroutines.flow.Flow
import kotlin.collections.List

interface StorageService {
    val sheets: Flow<List<Sheet>>
    suspend fun createSheet(sheet: Sheet)
    suspend fun joinSheet(sheetId: String)
    suspend fun createItem(sheet: Sheet, item: Item)
    suspend fun readSheet(sheetId: String): Sheet?
    suspend fun readAllSheets(): List<Sheet>
    suspend fun readAllItems(sheetId: String): List<Item>
    suspend fun updateSheet(sheet: Sheet)
    suspend fun updateItem(sheet: Sheet, item: Item)
    suspend fun deleteSheet(sheetId: String)
    suspend fun forgetSheet(sheetId: String)
    suspend fun deleteItem(sheet: Sheet, item: Item)
}