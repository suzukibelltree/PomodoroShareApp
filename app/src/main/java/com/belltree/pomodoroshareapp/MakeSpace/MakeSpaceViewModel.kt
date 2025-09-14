package com.belltree.pomodoroshareapp.MakeSpace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MakeSpaceViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    val userId: String = auth.currentUser?.uid ?: "Unknown"

    fun createSpace(space: Space) {
        viewModelScope.launch {
            spaceRepository.createSpace(space)
        }
    }

    suspend fun getCurrentUserById(): User {
        val u = userRepository.getUserById(userId)
        return User(
            userId = u?.userId ?: "Unknown",
            userName = u?.userName ?: "Unknown"
        )
    }
}