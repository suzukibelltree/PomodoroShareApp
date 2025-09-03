package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun addComment(spaceId: String, comment: Comment)
    fun getCommentsFlow(spaceId: String): Flow<List<Comment>>
    fun getMyCommentsFlow(userId: String): Flow<List<Comment>>
}