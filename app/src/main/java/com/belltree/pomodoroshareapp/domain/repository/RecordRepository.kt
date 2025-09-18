package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Record
import com.google.firebase.firestore.DocumentReference

interface RecordRepository {
    suspend fun getAllRecords(userId: String): List<Record>
    suspend fun getCurrentOneWeekRecords(userId: String): List<Record>
    fun addRecord(record: Record)
    suspend fun addRecordReturnDocRef(record: Record): DocumentReference
    suspend fun updateRecord(recordId: String, updatedData: Record)
    suspend fun getRecordsForRange(userId: String, startMillis: Long, endMillis: Long): List<Record>
}
