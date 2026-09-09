package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gastodiario_prefs", Context.MODE_PRIVATE)

    private val _monthlyBudget = MutableStateFlow(
        prefs.getFloat(KEY_MONTHLY_BUDGET, 2500.0f).toDouble()
    )
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
    )
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _dailyReminderTime = MutableStateFlow(
        prefs.getString(KEY_REMINDER_TIME, "20:30") ?: "20:30"
    )
    val dailyReminderTime: StateFlow<String> = _dailyReminderTime.asStateFlow()

    private val _themePreset = MutableStateFlow(
        runCatching {
            AppThemePreset.valueOf(prefs.getString(KEY_THEME_PRESET, AppThemePreset.EMERALD.name) ?: AppThemePreset.EMERALD.name)
        }.getOrDefault(AppThemePreset.EMERALD)
    )
    val themePreset: StateFlow<AppThemePreset> = _themePreset.asStateFlow()

    private val _themeMode = MutableStateFlow(
        runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setMonthlyBudget(budget: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        _monthlyBudget.value = budget
    }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
        _remindersEnabled.value = enabled
    }

    fun setDailyReminderTime(timeStr: String) {
        prefs.edit().putString(KEY_REMINDER_TIME, timeStr).apply()
        _dailyReminderTime.value = timeStr
    }

    fun setThemePreset(preset: AppThemePreset) {
        prefs.edit().putString(KEY_THEME_PRESET, preset.name).apply()
        _themePreset.value = preset
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    companion object {
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_THEME_PRESET = "theme_preset"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
