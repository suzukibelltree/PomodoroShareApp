package com.belltree.pomodoroshareapp.Space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.Record
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.repository.CommentRepository
import com.belltree.pomodoroshareapp.domain.repository.RecordRepository
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
class SpaceViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val commentRepository: CommentRepository,
    private val spaceRepository: SpaceRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    val userId: String = auth.currentUser?.uid ?: "Unknown"
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records
    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _space = MutableStateFlow<Space?>(null)
    val space: StateFlow<Space?> = _space

    private val _userNames = MutableStateFlow<List<String>>(emptyList())
    val userNames: StateFlow<List<String>> = _userNames

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun getCurrentUserById(): User {
        val u = userRepository.getUserById(userId)
        return User(
            userId = u?.userId ?: "Unknown",
            userName = u?.userName ?: "Unknown"
        )
    }

    fun addMyUserInfoToFirestore(spaceId: String) {
        viewModelScope.launch {
            spaceRepository.addMyUserInfoToSpace(
                spaceId = spaceId,
                userId = userId
            )
        }
    }

    fun fetchUserNames(userIds: List<String>) {
        viewModelScope.launch {
            val names = userIds.map { userId ->
                val u = userRepository.getUserById(userId)
                u?.userName ?: "Unknown"
            }
            _userNames.value = names
        }
    }


    fun getUnfinishedSpaces() {
        viewModelScope.launch {
            _spaces.value = spaceRepository.getUnfinishedSpaces()
        }
    }

    fun getSpaceById(spaceId: String) {
        viewModelScope.launch {
            _space.value = spaceRepository.getSpaceById(spaceId)
        }
    }

    fun addComment(spaceId: String, comment: Comment) {
        viewModelScope.launch {
            commentRepository.addComment(spaceId, comment)
        }
    }

    fun getComments(spaceId: String) {
        viewModelScope.launch {
            commentRepository.getCommentsFlow(spaceId)
                .collect { commentList ->
                    _comments.value = commentList
                }
        }
    }


    fun observeSpace(spaceId: String) {
        // 既存の監視をキャンセルしてから新しい Flow を収集
        viewModelScope.launch {
            spaceRepository.observeSpace(spaceId)
                .collect { latest ->
                    _space.value = latest
                    // 参加者リストが変更された場合、ユーザー名を再取得
                    latest?.let { space ->
                        fetchUserNames(space.participantsId)
                    }
                }
        }
    }
}