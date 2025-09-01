package com.belltree.pomodoroshareapp.domain.repository

import android.util.Log
import com.belltree.pomodoroshareapp.domain.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore

    fun addUserToFirestore(user: User) {
        db.collection("users").document(user.userId)
            .set(user)
            .addOnFailureListener { e -> Log.e("AuthRepository", "Failed to add user: $e") }
    }
}