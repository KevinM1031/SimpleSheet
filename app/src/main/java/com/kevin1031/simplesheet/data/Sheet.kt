package com.kevin1031.simplesheet.data

import com.google.firebase.firestore.DocumentId

data class Sheet(

    // ID
    @DocumentId val id: String = "",

    // Displayed data
    val title: String = "",
    val text: String = "",

    // Settings data
    val sortType: SheetSortType = SheetSortType.MainText,
    val view: SheetView = SheetView.PlainList,
    val fontSize: SheetFontSize = SheetFontSize.Medium,
    val maxLines: Int = 1,

    // Object info
    val dateCreated: Long = 0,
    val dateModified: Long = 0,

    // Permissions info
    val viewableBy: List<String> = listOf(),
    val editableBy: List<String> = listOf(),
)

enum class SheetView(val displayName: String) {
    PlainList("Plain list"),
    LabeledList("Labeled list"),
    DataList("Data list"),
    LabeledDataList("Labeled data list"),
    Notepad("Notepad"),
}

enum class SheetFontSize(val value: Int, val displayName: String) {
    Small(14, "Small"),
    Medium(20, "Medium"),
    Large(28, "Large"),
    ExtraLarge(36, "Extra large"),
}

enum class SheetSortType() {
    MainText,
    Label,
    Data,
    DateCreated,
    DateModified,
}