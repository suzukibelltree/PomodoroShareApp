package com.belltree.pomodoroshareapp.Setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel(
) {
    // DI 未導入のため内部生成（将来 Hilt へ移行予定）
    // 現在ログインしているユーザー
    private val _currentUser = mutableStateOf<FirebaseUser?>(authRepository.getCurrentUser())
    val currentUser: State<FirebaseUser?> = _currentUser

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isNewUser = mutableStateOf(false)
    val isNewUser: State<Boolean> = _isNewUser

    private val _goalStudyTime = mutableStateOf<Int?>(null)
    val goalStudyTime: State<Int?> = _goalStudyTime

    // サインアウトを行う関数
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _isNewUser.value = false
        _errorMessage.value = null
    }
    suspend fun updateUserProfiles(
        goalStudyTime: String
    ){
        _isLoading.value = true
        val uid = _currentUser.value?.uid
        val goal = goalStudyTime.toIntOrNull()

        if (uid == null || goal == null) {
            _errorMessage.value = "ユーザーIDまたは目標時間が不正です"
            _isLoading.value = false
            return
        }

        userRepository.updateUserToFirestore(
            userId = uid,
            updates = mapOf("goalStudyTime" to goal)
        )
        _goalStudyTime.value = goal
        _isLoading.value = false
    }

    fun loadCurrentUserGoal() {
        val uid = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            val user = userRepository.getUserById(uid)
            _goalStudyTime.value = user?.goalStudyTime
        }
    }
}
