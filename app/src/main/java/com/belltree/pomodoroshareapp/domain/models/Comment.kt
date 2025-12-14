package com.belltree.pomodoroshareapp.domain.models

data class Comment(
    val commentId: String = "",
    val spaceId: String = "",
    val userId: String = "",
    val userName: String = "",
    val photoUrl: String = "",
    val content: String = "",
    val postedAt: Long = System.currentTimeMillis()
)