package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.User

interface UserRepository {
    suspend fun addUserToFirestore(user: User)
    suspend fun getUserById(userId: String): User?
}