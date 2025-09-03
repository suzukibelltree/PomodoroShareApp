package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class SpaceRepositoryImpl : SpaceRepository{
    val db = Firebase.firestore

    // まだ終了していない部屋の一覧をFirestoreから取得する(HomeViewModelで使用)
    override suspend fun getUnfinishedSpaces(): List<Space> {
        val snapshot = db.collection("spaces")
            .whereNotEqualTo("spaceState", SpaceState.FINISHED)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Space::class.java) }
    }

    // 新しい部屋を作成する(MakeSpaceViewModelで使用)
    override fun createSpace(space: Space){
        db.collection("spaces").add(space)
    }

    // 部屋に入ったときに部屋の情報を取得する(SpaceViewModelで使用)
    override suspend fun getSpaceById(spaceId: String): Space? {
        val snapshot = db.collection("spaces").document(spaceId).get().await()
        return snapshot.toObject(Space::class.java)
    }
}