package com.belltree.pomodoroshareapp.Space.Comment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.repository.CommentRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel(
    private val commentRepository: CommentRepositoryImpl
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

     fun getCommentsFlow(spaceId:String): Flow<List<Comment>> {
        return commentRepository.getCommentsFlow(spaceId)
    }

    fun addComment(comment: Comment, spaceId: String) {
        viewModelScope.launch {
            commentRepository.addComment(spaceId, comment)
        }
    }

    fun getMyCommentsFlow(userId: String): Flow<List<Comment>> {
        return commentRepository.getMyCommentsFlow(userId)
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

}
