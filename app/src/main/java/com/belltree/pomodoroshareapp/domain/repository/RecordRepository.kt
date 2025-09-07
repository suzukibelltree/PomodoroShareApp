package com.belltree.pomodoroshareapp.domain.repository

import com.belltree.pomodoroshareapp.domain.models.Record


interface RecordRepository {
    suspend fun getAllRecords(userId: String): List<Record>
    suspend fun getCurrentOneWeekRecords(userId: String): List<Record>
    fun addRecord(record: Record)
    fun updateRecord(recordId: String, updatedData: Record)
}