package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Record
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class RecordRepositoryImpl: RecordRepository {
    val db = Firebase.firestore

    // そのユーザーの全レコードを取得(RecordViewModelで使用する)
    override suspend fun getAllRecords(userId: String): List<Record> {
        val snapshot = db.collection("records")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Record::class.java) }
    }

    override fun addRecord(record: Record) {
        db.collection("records").add(record)
    }

    override fun updateRecord(recordId: String, updatedData: Record) {
        db.collection("records").document(recordId).update(
            mapOf(
                "endTime" to updatedData.endTime,
                "durationMinutes" to updatedData.durationMinutes,
                "taskDescription" to updatedData.taskDescription
            )
        )
    }


}