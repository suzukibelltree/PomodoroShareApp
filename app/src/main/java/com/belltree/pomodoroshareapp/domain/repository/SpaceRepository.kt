package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

interface SpaceRepository {
    suspend fun getUnfinishedSpaces(): List<Space>
    fun createSpace(space: Space)
    suspend fun getSpaceById(spaceId: String): Space?
}