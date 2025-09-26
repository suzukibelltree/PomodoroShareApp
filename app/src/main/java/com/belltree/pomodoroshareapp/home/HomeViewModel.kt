package com.belltree.pomodoroshareapp.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.RewardState
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
import kotlinx.coroutines.Job

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
    private val _ownerRewardState = MutableStateFlow<String>(RewardState.Bronze.toString())
    val ownerRewardState: StateFlow<String> = _ownerRewardState
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

    private var spacesObserverJob: Job? = null

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _spaces.value = spaceRepository.getUnfinishedSpaces()
            _isLoading.value = false
        }
        // Start/Restart realtime observation in separate job (non-blocking)
        spacesObserverJob?.cancel()
        spacesObserverJob = viewModelScope.launch {
            spaceRepository.observeUnfinishedSpaces().collect { list ->
                _spaces.value = list
                // BREAK -> FINISHED を導出して反映（ホスト不在/参加者ゼロでも完了させる）
                val workDuration = 25 * 60 * 1000L
                val breakDuration = 5 * 60 * 1000L
                val cycleLength = workDuration + breakDuration
                val now = System.currentTimeMillis()
                list.forEach { sp ->
                    val timeUntilStart = sp.startTime - now
                    if (timeUntilStart <= 0) {
                        val elapsed = (now - sp.startTime).coerceAtLeast(0L)
                        val currentCycle = (elapsed / cycleLength).toInt()
                        val shouldBeFinished = currentCycle >= sp.sessionCount
                        if (shouldBeFinished && sp.spaceState != com.belltree.pomodoroshareapp.domain.models.SpaceState.FINISHED) {
                            viewModelScope.launch {
                                spaceRepository.updateSpace(
                                    sp.spaceId,
                                    mapOf(
                                        "spaceState" to com.belltree.pomodoroshareapp.domain.models.SpaceState.FINISHED.name,
                                        "currentSessionCount" to sp.sessionCount,
                                        "lastUpdated" to System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                }
            }
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
            _ownerRewardState.value = u?.rewardState.toString() ?: RewardState.Bronze.toString()
        }
    }

    fun markRecentlyLeft(spaceId: String) {
        Log.w("HomeViewModel", "markRecentlyLeft id=$spaceId")
        recentlyLeftSpaceManager.mark(spaceId)
    }
}

