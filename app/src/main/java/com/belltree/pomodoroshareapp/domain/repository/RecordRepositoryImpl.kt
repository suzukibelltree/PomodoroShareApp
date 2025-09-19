package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Record
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class RecordRepositoryImpl @Inject constructor(val db: FirebaseFirestore) : RecordRepository {

    // そのユーザーの全レコードを取得(RecordViewModelで使用する)
    override suspend fun getAllRecords(userId: String): List<Record> {
        val snapshot = db.collection("records").whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Record::class.java) }
    }

    // そのユーザーの過去1週間のレコードを取得(RecordViewModelで使用する)
    override suspend fun getCurrentOneWeekRecords(userId: String): List<Record> {
        val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        val snapshot =
            db.collection("records")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("startTime", oneWeekAgo)
                .get()
                .await()
        return snapshot.documents.mapNotNull { it.toObject(Record::class.java) }
    }

    override fun addRecord(record: Record) {
        db.collection("records").add(record)
    }

    override suspend fun addRecordReturnDocRef(record: Record): DocumentReference {
        val docRef = db.collection("records").document()
        docRef.set(record).await()
        return docRef
    }

    override suspend fun updateRecord(recordId: String, updatedData: Record) {
        db.collection("records")
            .document(recordId)
            .update(
                mapOf(
                    "endTime" to updatedData.endTime,
                    "durationMinutes" to updatedData.durationMinutes,
                    "taskDescription" to updatedData.taskDescription
                )
            )
            .await()
    }

    // 指定した期間内のレコードを取得(RecordViewModelで使用する)
    override suspend fun getRecordsForRange(
        userId: String,
        startMillis: Long,
        endMillis: Long
    ): List<Record> {
        val snapshot = db.collection("records")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startTime", startMillis)
            .whereLessThan("startTime", endMillis)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(Record::class.java) }
    }

    override suspend fun getRecordsForMonth(
        userId: String,
        startMillis: Long,
        todayEnd: Long
    ): List<Record> {
        val snapshot = db.collection("records")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("endTime", startMillis)
            .whereLessThan("endTime", todayEnd)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(Record::class.java) }
    }

}
