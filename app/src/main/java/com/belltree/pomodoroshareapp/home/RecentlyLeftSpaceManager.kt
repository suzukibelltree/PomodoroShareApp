package com.belltree.pomodoroshareapp.home

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class RecentlyLeftSpaceManager @Inject constructor() {
    private val _recentlyLeftSpaceId = MutableStateFlow<String?>(null)
    val recentlyLeftSpaceId: StateFlow<String?> = _recentlyLeftSpaceId

    fun mark(spaceId: String) {
        _recentlyLeftSpaceId.value = spaceId
    }

    fun clear() {
        _recentlyLeftSpaceId.value = null
    }
}


