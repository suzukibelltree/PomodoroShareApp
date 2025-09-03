package com.belltree.pomodoroshareapp.domain.repository

import android.util.Log
import com.belltree.pomodoroshareapp.domain.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepository{
    private val db = Firebase.firestore

    override suspend fun addUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.userId).set(user).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to add user", e)
            throw e
        }
    }
}