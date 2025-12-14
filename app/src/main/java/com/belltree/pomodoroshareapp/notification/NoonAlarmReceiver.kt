package com.belltree.pomodoroshareapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.belltree.pomodoroshareapp.domain.repository.RecordRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
@AndroidEntryPoint
class NoonAlarmReceiver : BroadcastReceiver() {

    //通知の作成と表示に関するヘルパー関数、オブジェクト関数
    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var recordRepository: RecordRepository

    @Inject
    lateinit var auth: FirebaseAuth
    //通信を受け取り通知をユーザーに送る
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // 端末再起動後にリスケジュールのみ行う
            NoonAlarmScheduler.scheduleDailyNoon(context)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val zone = ZoneId.of("Asia/Tokyo")
                    val today = LocalDate.now(zone)
                    val startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
                    val endMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    val records = recordRepository.getRecordsForRange(userId, startMillis, endMillis)
                    val totalMinutes = records.sumOf { it.durationMinutes }
                    notificationHelper.showNotification(
                        title = "お知らせ",
                        message = "今日は${totalMinutes}分勉強しました！"
                    )
                } else {
                    notificationHelper.showNotification(
                        title = "お知らせ",
                        message = "今日は勉強記録がありません。"
                    )
                }
            } catch (e: Exception) {
                notificationHelper.showNotification(
                    title = "お知らせ",
                    message = "今日の勉強時間の取得に失敗しました"
                )
            } finally {
                NoonAlarmScheduler.scheduleDailyNoon(context)
                pendingResult.finish()
            }
        }
    }
}


