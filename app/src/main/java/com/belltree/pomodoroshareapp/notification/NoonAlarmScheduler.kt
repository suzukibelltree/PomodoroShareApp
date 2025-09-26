package com.belltree.pomodoroshareapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import java.util.TimeZone

object NoonAlarmScheduler {

    private const val REQUEST_CODE_NOON = 120000

    fun scheduleDailyNoon(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context)

        //Tokyoのタイムゾーンで12:00に設定している
        val triggerTime = nextNoonInMillis()

        //APIレベルに応じて適切なメソッドを使用して起動時間を登録
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ では正確なアラームの権限が必要
                if (alarmManager.canScheduleExactAlarms()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } else {
                    // 許可がない場合は近似アラームでフォールバック（クラッシュ回避）
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (se: SecurityException) {
            // 権限がなく例外が発生した場合も近似アラームでフォールバック
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    //コンテキストによりキャンセルすべきタイマーを特定
    fun cancelDailyNoon(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context)
        alarmManager.cancel(pendingIntent)
    }

    //NoonAlarmReceiver起動のためにIntentを設定
    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, NoonAlarmReceiver::class.java)
        //Intentに設定を追加
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE_NOON, intent, flags)
    }

    //何時に通知が来るか指定している
    private fun nextNoonInMillis(
    ): Long {
        val cal = Calendar.getInstance(
            TimeZone.getTimeZone("Asia/Tokyo")
        )
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}


