package com.example.simplesheet.data

import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String
    fun hasUser(): Boolean
    fun getUserProfile(): User
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}
