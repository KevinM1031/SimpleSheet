package com.kevin1031.simplesheet.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl @Inject constructor() : AccountService {

    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser.toAppUser())
                }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    override val currentUserId: String
        get() = Firebase.auth.currentUser?.uid.orEmpty()

    override fun hasUser(): Boolean {
        return Firebase.auth.currentUser != null
    }

    override fun getUserProfile(): User {
        return Firebase.auth.currentUser.toAppUser()
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(firebaseCredential).await()

        Firebase.auth.addAuthStateListener { authState ->
            val user = authState.currentUser
            if (user != null) {

                // User is signed in
                val userId = user.uid
                val userRef = Firebase.firestore.collection("users").document(userId)

                // Check if the user document already exists
                userRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val newUser = UserData(
                            uid = userId,
                            sheets = listOf(),
                        )
                        userRef.set(newUser)
                        Log.d("AccountServiceImpl", "User document created successfully")
                    }
                }
            }
        }
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
    }

    override suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
    }

    private fun FirebaseUser?.toAppUser(): User {
        return if (this == null) User() else User(
            uid = this.uid,
            gmail = this.email ?: "",
        )
    }
}