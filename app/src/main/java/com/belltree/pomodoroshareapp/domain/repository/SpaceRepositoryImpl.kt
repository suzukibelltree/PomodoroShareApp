package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.domain.models.TimerState
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SpaceRepositoryImpl @Inject constructor(
    val db: FirebaseFirestore
) : SpaceRepository {

    // まだ終了していない部屋の一覧をFirestoreから取得する(HomeViewModelで使用)
    override suspend fun getUnfinishedSpaces(): List<Space> {
        val snapshot = db.collection("spaces")
            .whereNotEqualTo("spaceState", SpaceState.FINISHED)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toSpace() }
    }

    // 新しい部屋を作成する(MakeSpaceViewModelで使用)
    override fun createSpace(space: Space) {
        db.collection("spaces").add(space)
    }

    override suspend fun createSpaceReturnId(space: Space): String {
        val docRef = db.collection("spaces").add(space).await()
        return docRef.id
    }

    // 部屋に入ったときに部屋の情報を取得する(SpaceViewModelで使用)
    override suspend fun getSpaceById(spaceId: String): Space? {
        val snapshot = db.collection("spaces").document(spaceId).get().await()
        return snapshot.toSpace()
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
                snapshot?.toSpace()?.let { space ->
                    trySend(space)
                }
            }
        awaitClose { listener.remove() }
    }

    // Manual mapping to tolerate legacy field names and types in Firestore
    private fun DocumentSnapshot.toSpace(): Space? {
        val data = data ?: return null
        fun <T> getField(key: String): T? = data[key] as? T

        val spaceId: String = id
        val ownerId: String = getField("ownerId") ?: ""
        val spaceName: String = getField("spaceName") ?: ""

        val ownerName: String = getField("ownerName") ?: ""

        val spaceStateStr: String? = getField("spaceState")
        val spaceState: SpaceState = spaceStateStr?.let {
            runCatching { SpaceState.valueOf(it) }.getOrElse { SpaceState.WAITING }
        } ?: SpaceState.WAITING

        val timerStateStr: String? = getField("timerState")
        val timerState: TimerState = timerStateStr?.let {
            runCatching { TimerState.valueOf(it) }.getOrElse { TimerState.STOPPED }
        } ?: TimerState.STOPPED

        val startTime: Long = (getField<Number>("startTime")?.toLong()) ?: 0L
        val sessionCount: Int = (getField<Number>("sessionCount")?.toInt()) ?: 0

        // Accept both "participantsId" (current) and "participantId" (legacy)
        val participants: List<*>? = getField("participantsId") ?: getField("participantId")
        val participantsId: List<String> = participants?.mapNotNull { it as? String } ?: emptyList()

        val createdAt: Long = (getField<Number>("createdAt")?.toLong()) ?: 0L
        val lastUpdated: Long = (getField<Number>("lastUpdated")?.toLong()) ?: 0L

        // Accept both "isPrivate" (current) and "private" (legacy)
        val isPrivate: Boolean = getField("isPrivate")
            ?: getField<Boolean>("private")
            ?: false

        return Space(
            spaceId = spaceId,
            ownerId = ownerId,
            ownerName = ownerName,
            spaceName = spaceName,
            spaceState = spaceState,
            timerState = timerState,
            startTime = startTime,
            sessionCount = sessionCount,
            participantsId = participantsId,
            createdAt = createdAt,
            lastUpdated = lastUpdated,
            isPrivate = isPrivate,
        )
    }
}