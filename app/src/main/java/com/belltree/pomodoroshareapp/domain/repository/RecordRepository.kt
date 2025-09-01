package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Record
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class RecordRepository {
    val db = Firebase.firestore

    // そのユーザーの全レコードを取得(RecordViewModelで使用する)
    suspend fun getAllRecords(userId: String): List<Record> {
        val snapshot = db.collection("records")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Record::class.java) }
    }

    // 1セクション目終了時に新しくレコードを保存(SpaceViewModelで使用する)
    fun addRecord(record: Record) = db.collection("records").add(record)

    // 2セクション目行以降に既存のレコードを更新(SpaceViewModelで使用する)
    fun updateRecord(recordId: String, updatedData: Record) =
        db.collection("records").document(recordId).update(
            mapOf(
                "endTime" to updatedData.endTime,
                "durationMinutes" to updatedData.durationMinutes,
                "taskDescription" to updatedData.taskDescription
            )
        )


}