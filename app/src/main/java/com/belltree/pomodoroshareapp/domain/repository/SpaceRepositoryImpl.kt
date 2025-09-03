package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SpaceRepositoryImpl : SpaceRepository {
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
    override fun createSpace(space: Space) {
        db.collection("spaces").add(space)
    }

    // 部屋に入ったときに部屋の情報を取得する(SpaceViewModelで使用)
    override suspend fun getSpaceById(spaceId: String): Space? {
        val snapshot = db.collection("spaces").document(spaceId).get().await()
        return snapshot.toObject(Space::class.java)
    }

    // 部屋画面遷移時に自分のユーザーIDを参加者リストに追加する(SpaceViewModelで使用)
    override fun addMyUserInfoToSpace(spaceId: String, userId: String) {
        val spaceRef = db.collection("spaces").document(spaceId)
        spaceRef.update("participantsId", FieldValue.arrayUnion(userId))
    }

    // 部屋に参加中のユーザーのIDリストを監視する(SpaceViewModelで使用)
    override fun observeSpace(spaceId: String): Flow<Space> = callbackFlow<Space> {
        val listener = db.collection("spaces")
            .document(spaceId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                snapshot?.toObject(Space::class.java)?.let { space ->
                    trySend(space)
                }
            }
        awaitClose { listener.remove() }
    }
}