package com.belltree.pomodoroshareapp.Contact

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.domain.repository.AuthRepository
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _currentUser = mutableStateOf(authRepository.getCurrentUser())
    val currentUser: State<com.google.firebase.auth.FirebaseUser?> = _currentUser

    init {
        loadSpaces()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _currentUser.value = authRepository.getCurrentUser()
    }

    fun loadSpaces() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val allSpaces = spaceRepository.getUnfinishedSpaces()
                _spaces.value = allSpaces

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSpaces() {
        loadSpaces()
    }

    fun getUserDisplayName(): String {
        return _currentUser.value?.displayName ?: "ユーザー"
    }
}
