package com.belltree.pomodoroshareapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
@AndroidEntryPoint
class NoonAlarmReceiver : BroadcastReceiver() {

    //通知の作成と表示に関するヘルパー関数、オブジェクト関数
    @Inject
    lateinit var notificationHelper: NotificationHelper
    //通信を受け取り通知をユーザーに送る
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // 端末再起動後にリスケジュールのみ行う
            NoonAlarmScheduler.scheduleDailyNoon(context)
            return
        }

        notificationHelper.showNotification(
            title = "お知らせ",
            message = "12:00になりました！今日の目標に向けて頑張ろう！"
        )

        // 次回分を再スケジュール
        NoonAlarmScheduler.scheduleDailyNoon(context)
    }
}


