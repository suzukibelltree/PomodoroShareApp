package com.belltree.pomodoroshareapp.domain.repository

import android.util.Log
import com.belltree.pomodoroshareapp.domain.models.User
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    override suspend fun addUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.userId).set(user).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to add user", e)
            throw e
        }
    }
    override suspend fun updateUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.userId).set(user).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to update user", e)
            throw e
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to get user", e)
            null
        }
    }
}