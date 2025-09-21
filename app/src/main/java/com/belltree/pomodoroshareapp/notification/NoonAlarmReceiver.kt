package com.belltree.pomodoroshareapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class NoonAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        notificationHelper.showNotification(
            title = "お知らせ",
            message = "12:00になりました！今日の目標に向けて頑張ろう！"
        )
        // Re-schedule next day's alarm to ensure persistence across device time changes, etc.
        NoonAlarmScheduler.scheduleDailyNoon(context)
    }
}


