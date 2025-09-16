package com.belltree.pomodoroshareapp.Record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.BuildConfig
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.DailyStudySummary
import com.belltree.pomodoroshareapp.domain.models.Record
import com.belltree.pomodoroshareapp.domain.repository.CommentRepository
import com.belltree.pomodoroshareapp.domain.repository.RecordRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val commentRepository: CommentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    val userId: String = auth.currentUser?.uid ?: "Unknown"
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val apiKey = BuildConfig.API_KEY
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = apiKey
    )

    // Geminiからのメッセージ
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // AIによるメッセージを取得(画面遷移時に呼び出す)
    fun generateMessage(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentRecords = recordRepository.getCurrentOneWeekRecords(userId)
                val weeklySummary = aggregateByDay(currentRecords)
                    .joinToString(separator = "\n") { "${it.date}: ${it.totalMinutes}分" }
                val prompt =
                    "直近1週間の勉強時間:\n$weeklySummary\nこれを踏まえて、簡潔にアドバイスをください。"
                val response = generativeModel.generateContent(prompt)
                _message.value = response.text ?: "No response"
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
        _isLoading.value = false
    }

    fun aggregateByDay(records: List<Record>): List<DailyStudySummary> {
        val today = LocalDate.now()
        val lastWeek = (0..6).map { today.minusDays(it.toLong()) }.reversed()

        // セッションを日ごとに合計
        val dailyTotals = records.groupBy { record ->
            Instant.ofEpochMilli(record.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { (_, sessions) ->
            sessions.sumOf { it.durationMinutes }
        }

        // 直近1週間分すべての日付を含め、存在しない日は0で埋める
        return lastWeek.map { date ->
            DailyStudySummary(
                date = date,
                totalMinutes = dailyTotals[date] ?: 0
            )
        }
    }


    fun updateRecord(recordId: String, updatedData: Record) {
        viewModelScope.launch {
            recordRepository.updateRecord(recordId, updatedData)
        }
    }

    fun getAllRecords(userId: String) {
        viewModelScope.launch {
            _records.value = recordRepository.getAllRecords(userId)
        }
    }

    fun addRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.addRecord(record)
        }
    }
}