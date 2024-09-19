package com.example.simplesheet.data

import com.google.firebase.firestore.DocumentId

data class UserData(
    val uid: String = "",
    val sheets: List<String> = emptyList(),
)
