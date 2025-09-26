package com.belltree.pomodoroshareapp.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val recentlyLeftSpaceManager: RecentlyLeftSpaceManager,
) : ViewModel() {
    val userId: String = auth.currentUser?.uid ?: "Unknown"

    private val _ownerName = MutableStateFlow<String>("")
    val ownerName: StateFlow<String> = _ownerName
    private val _ownerPhotoUrl = MutableStateFlow<String>("")
    val ownerPhotoUrl: StateFlow<String> = _ownerPhotoUrl
    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    // 個別取得用の選択中スペース
    private val _selectedSpace = MutableStateFlow<Space?>(null)
    val selectedSpace: StateFlow<Space?> = _selectedSpace

    suspend fun getUnfinishedSpaces(): List<Space> {
        _spaces.value = spaceRepository.getUnfinishedSpaces()
        return _spaces.value
    }

    suspend fun getSpaceById(spaceId: String): Space? {
        val space = spaceRepository.getSpaceById(spaceId)
        if (space == null) {
            Log.w("HomeViewModel", "getSpaceById: not found for id=$spaceId")
        } else {
            Log.w("HomeViewModel", "getSpaceById: found ${'$'}{space.spaceName}")
        }
        _selectedSpace.value = space
        return space
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val recentlyLeftSpaceId: StateFlow<String?> = recentlyLeftSpaceManager.recentlyLeftSpaceId

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _spaces.value = spaceRepository.getUnfinishedSpaces()
            _isLoading.value = false
        }
    }

    suspend fun getCurrentUserById(): User {
        val u = userRepository.getUserById(userId)
        return User(
            userId = u?.userId ?: "Unknown",
            userName = u?.userName ?: "Unknown",
            photoUrl = u?.photoUrl ?: ""
        )
    }

    suspend fun getUserById(userId: String): User?{
        return userRepository.getUserById(userId)
    }

    fun loadOwner() {
        viewModelScope.launch {
            val u = userRepository.getUserById(userId)
            _ownerName.value = u?.userName ?: "Guest User"
            _ownerPhotoUrl.value = u?.photoUrl ?: ""
        }
    }

    fun markRecentlyLeft(spaceId: String) {
        Log.w("HomeViewModel", "markRecentlyLeft id=$spaceId")
        recentlyLeftSpaceManager.mark(spaceId)
    }
}

