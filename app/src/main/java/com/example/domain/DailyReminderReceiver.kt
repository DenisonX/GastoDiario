package com.example.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        when (intent.action) {
            DailyReminderScheduler.ACTION_DAILY_REMINDER -> {
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        sendReminderWithTodayData(appContext)
                    } finally {
                        DailyReminderScheduler.scheduleFromPreferences(appContext)
                        pendingResult.finish()
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> DailyReminderScheduler.scheduleFromPreferences(appContext)
        }
    }

    private suspend fun sendReminderWithTodayData(context: Context) {
        if (!UserPreferencesManager(context).remindersEnabled.value) return

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayExpenses = AppDatabase.getInstance(context).expenseDao().getExpensesSince(startOfToday)
        val totalToday = todayExpenses.sumOf { it.amount }
        val topCategory = todayExpenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .maxByOrNull { it.value }

        NotificationHelper.sendDailyExpenseReminder(
            context,
            topCategory = topCategory?.key,
            topCategoryAmount = topCategory?.value ?: 0.0,
            totalSpentToday = totalToday
        )
    }
}
