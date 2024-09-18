package com.kevin1031.simplesheet.data

import com.google.firebase.firestore.DocumentId

data class Item(
    @DocumentId val id: String = "",

    val text: String = "",
    val label: String = "",
    val data: String = "",

    val dateCreated: Long = 0,
    val dateModified: Long = 0,
)
