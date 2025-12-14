package com.belltree.pomodoroshareapp.domain.models

data class Space(
    val spaceId: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val spaceName: String = "",
    val spaceState: SpaceState = SpaceState.WAITING,
    val timerState: TimerState = TimerState.STOPPED,
    val startTime: Long = 0L,
    val sessionCount: Int = 0,
    val currentSessionCount: Int = 0,
    val participantsId: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val lastUpdated: Long = 0L,
    val isPrivate: Boolean = false,
)

enum class SpaceState {
    WAITING,
    WORKING,
    BREAK,
    FINISHED,
}

enum class TimerState {
    STARTED,
    PAUSED,
    STOPPED,
}
