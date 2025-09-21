package com.belltree.pomodoroshareapp.domain.models

data class User(
    val userId: String = "",
    val userName: String = "",
    val photoUrl: String = "",
    val goalStudyTime: Long = 0,
    val currentStudyTime: Long = 0,
)