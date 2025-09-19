package com.belltree.pomodoroshareapp.Record

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.BuildConfig
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.DailyStudySummary
import com.belltree.pomodoroshareapp.domain.models.Record
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
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.TimeZone


@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    val userId: String = auth.currentUser?.uid ?: "Unknown"
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records

    private val _weeklySummaryForGraph = MutableStateFlow<List<DailyStudySummary>>(emptyList())
    val weeklySummaryForGraph: StateFlow<List<DailyStudySummary>> = _weeklySummaryForGraph

    // 0 = 今週, -1 = 先週のように扱う
    private val _currentWeeklyOffset = MutableStateFlow(0)
    val currentWeeklyOffset: StateFlow<Int> = _currentWeeklyOffset

    private val _monthlySummaryForGraph = MutableStateFlow<List<DailyStudySummary>>(emptyList())
    val monthlySummaryForGraph: StateFlow<List<DailyStudySummary>> = _monthlySummaryForGraph

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

//    fun getCurrentOneWeekRecords(userId: String) {
//        viewModelScope.launch {
//            val weeklyRecords = recordRepository.getCurrentOneWeekRecords(userId)
//            _weeklySummaryForGraph.value = generateLastWeekSummary(weeklyRecords)
//        }
//    }

    fun addRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.addRecord(record)
        }
    }

    fun generateLastWeekSummary(
        records: List<Record>,
        startMillis: Long,
        endMillis: Long
    ): List<DailyStudySummary> {
        val startDate =
            Instant.ofEpochMilli(startMillis).atZone(ZoneId.of("Asia/Tokyo")).toLocalDate()
        val endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.of("Asia/Tokyo")).toLocalDate()

        // endTimeからLocalDateに変換して、対象期間のみフィルタ
        val filtered = records.filter { history ->
            val date = Instant.ofEpochMilli(history.endTime)
                .atZone(ZoneId.of("Asia/Tokyo"))
                .toLocalDate()
            !date.isBefore(startDate) && !date.isAfter(endDate)
        }

        // 日付ごとにdurationMinutesを合計
        val totalsByDate: Map<LocalDate, Int> = filtered.groupBy { history ->
            Instant.ofEpochMilli(history.endTime)
                .atZone(ZoneId.of("Asia/Tokyo"))
                .toLocalDate()
        }.mapValues { entry ->
            entry.value.sumOf { it.durationMinutes }
        }

        val days = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        // 期間内の全日付リストを生成し、データがなければ0で埋める
        return (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            DailyStudySummary(date, totalsByDate[date] ?: 0)
        }
    }

    fun getOneWeekRecords() {
        viewModelScope.launch {
            val (startOfWeek, endOfWeek) = getWeekRangeByOffset(_currentWeeklyOffset.value)
            val result = recordRepository.getRecordsForRange(userId, startOfWeek, endOfWeek)
            val summary = generateLastWeekSummary(result, startOfWeek, endOfWeek)
            _weeklySummaryForGraph.value = summary
            Log.d("hogehoge", " summary: $summary")
        }
    }

    fun moveToPreviousWeek() {
        _currentWeeklyOffset.value -= 1
        getOneWeekRecords()
    }

    fun moveToNextWeek() {
        _currentWeeklyOffset.value += 1
        getOneWeekRecords()
    }

    fun getWeekRangeByOffset(offset: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // offset分だけ週を移動
        calendar.add(Calendar.WEEK_OF_YEAR, offset)
        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = calendar.timeInMillis

        return startOfWeek to endOfWeek
    }


    fun loadMonthlySummary(yearMonth: YearMonth) {
        viewModelScope.launch {
            val zone = ZoneId.of("Asia/Tokyo")
            val startOfMonth = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val todayEnd = LocalDateTime.now().atZone(zone).toInstant().toEpochMilli()

            val result = recordRepository.getRecordsForMonth(userId, startOfMonth, todayEnd)
            val summary = generateMonthlySummary(result, yearMonth)

            _monthlySummaryForGraph.value = summary
        }
    }

    fun generateMonthlySummary(records: List<Record>, month: YearMonth): List<DailyStudySummary> {
        val zone = ZoneId.of("Asia/Tokyo")
        val startOfMonth = month.atDay(1)
        val today = LocalDate.now(zone)
        // その月の最終日か、今日のどちらか早い方を「集計の終了日」にする
        val endDate = minOf(month.atEndOfMonth(), today)

        val filtered = records.filter { record ->
            val date = Instant.ofEpochMilli(record.endTime).atZone(zone).toLocalDate()
            !date.isBefore(startOfMonth) && !date.isAfter(endDate)
        }

        val totalsByDate: Map<LocalDate, Int> = filtered.groupBy { record ->
            Instant.ofEpochMilli(record.endTime).atZone(zone).toLocalDate()
        }.mapValues { entry -> entry.value.sumOf { it.durationMinutes } }

        return (1..endDate.dayOfMonth).map { day ->
            val date = month.atDay(day)
            DailyStudySummary(date, totalsByDate[date] ?: 0)
        }
    }


}