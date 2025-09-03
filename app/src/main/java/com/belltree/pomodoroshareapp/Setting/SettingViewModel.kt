package com.belltree.pomodoroshareapp.Setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
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

    // サインアウトを行う関数
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _isNewUser.value = false
        _errorMessage.value = null
    }
}
