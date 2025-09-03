package com.belltree.pomodoroshareapp.domain.repository

import android.util.Log
import com.belltree.pomodoroshareapp.domain.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

interface UserRepository {
    suspend fun addUserToFirestore(user: User)
}