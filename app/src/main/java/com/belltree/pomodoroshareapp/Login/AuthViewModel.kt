package com.belltree.pomodoroshareapp.Login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import jakarta.inject.Inject


/**
 * 認証関連の状態と操作を管理するViewModel
 * UI側ではcurrentUserとauthStateを監視し、認証操作を呼び出す
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
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

    // 匿名認証を行う関数
    fun signInAnonymously() {
        _isLoading.value = true
        authRepository.signInAnonymously { success, error ->
            _isLoading.value = false
            if (success) {
                _currentUser.value = authRepository.getCurrentUser()
                _currentUser.value?.let { user ->
                    viewModelScope.launch {
                        userRepository.addUserToFirestore(
                            User(userId = user.uid, userName = user.displayName ?: "Guest User")
                        )
                    }
                }
            } else {
                _errorMessage.value = "匿名認証に失敗しました: $error"
            }
        }
    }

    fun onLoginSuccess(user: FirebaseUser?) {
        _currentUser.value = user
        _errorMessage.value = null

        // Firestore にユーザーを追加
        viewModelScope.launch {
            userRepository.addUserToFirestore(
                User(userId = user?.uid ?: "", userName = user?.displayName ?: "Guest User")
            )
        }
    }

    fun onLoginFailed(message: String) {
        _currentUser.value = null
        _errorMessage.value = message
    }

    // Google サインイン（ID Token を受け取りFirebase認証）
    fun signInWithGoogle(idToken: String) {
        _isLoading.value = true
        authRepository.signInWithGoogle(idToken) { success, isNew, error ->
            _isLoading.value = false
            if (success) {
                _isNewUser.value = isNew
                onLoginSuccess(authRepository.getCurrentUser())
            } else {
                onLoginFailed(error ?: "Googleサインインに失敗しました")
            }
        }
    }


    // サインアウトを行う関数
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _isNewUser.value = false
        _errorMessage.value = null
    }
}
