package com.example.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.UserPreferencesManager
import java.util.Calendar
import java.util.TimeZone

/**
 * Schedules the daily expense reminder with AlarmManager.
 *
 * Uses an inexact, Doze-friendly alarm (setAndAllowWhileIdle), so it does not need the
 * SCHEDULE_EXACT_ALARM permission. The system may deliver it a few minutes late.
 * Each delivery reschedules the next day in DailyReminderReceiver.
 */
object DailyReminderScheduler {

    const val ACTION_DAILY_REMINDER = "com.example.action.DAILY_EXPENSE_REMINDER"
    private const val REQUEST_CODE = 2001
    private const val DEFAULT_HOUR = 20
    private const val DEFAULT_MINUTE = 30

    fun scheduleFromPreferences(context: Context) {
        val prefs = UserPreferencesManager(context)
        schedule(context, prefs.remindersEnabled.value, prefs.dailyReminderTime.value)
    }

    fun schedule(context: Context, enabled: Boolean, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = reminderPendingIntent(context)

        alarmManager.cancel(pendingIntent)
        if (!enabled) return

        val (hour, minute) = parseTime(timeStr)
        val triggerAt = nextTriggerMillis(System.currentTimeMillis(), hour, minute)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()
        val minute = parts.getOrNull(1)?.toIntOrNull()
        return if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
            hour to minute
        } else {
            DEFAULT_HOUR to DEFAULT_MINUTE
        }
    }

    fun nextTriggerMillis(
        nowMillis: Long,
        hour: Int,
        minute: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
