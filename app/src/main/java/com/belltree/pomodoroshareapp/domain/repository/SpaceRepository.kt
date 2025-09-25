package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Space
import kotlinx.coroutines.flow.Flow

interface SpaceRepository {
    suspend fun getUnfinishedSpaces(): List<Space>
    fun createSpace(space: Space)
    suspend fun createSpaceReturnId(space: Space): String
    suspend fun getSpaceById(spaceId: String): Space?
    fun addMyUserInfoToSpace(spaceId: String, userId: String)
    fun observeSpace(spaceId: String): Flow<Space>
}