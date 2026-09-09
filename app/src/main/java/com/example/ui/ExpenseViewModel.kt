package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.UserPreferencesManager
import com.example.domain.ExpenseCategory
import com.example.domain.ExpenseCategorizer
import com.example.domain.NotificationHelper
import com.example.domain.ParsedExpense
import com.example.domain.ReceiptOcrScanner
import com.example.domain.ReceiptScanResult
import com.example.domain.VoiceExpenseParser
import com.example.domain.WeeklyReportExporter
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val monthlyBudget: Double = 2500.0,
    val budgetProgress: Float = 0f,
    val topTodayCategory: ExpenseCategory? = null,
    val topTodayAmount: Double = 0.0,
    val categoryTotalsThisMonth: Map<ExpenseCategory, Double> = emptyMap(),
    val dailySpendLast7Days: List<DaySpend> = emptyList(),
    val isProcessingOcr: Boolean = false,
    val lastOcrResult: ReceiptScanResult? = null
)

data class DaySpend(
    val dayLabel: String,
    val amount: Double,
    val isToday: Boolean = false
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val preferencesManager = UserPreferencesManager(application)

    val allExpenses: StateFlow<List<ExpenseEntity>>
    val monthlyBudget: StateFlow<Double> = preferencesManager.monthlyBudget
    val remindersEnabled: StateFlow<Boolean> = preferencesManager.remindersEnabled
    val dailyReminderTime: StateFlow<String> = preferencesManager.dailyReminderTime
    val themePreset: StateFlow<AppThemePreset> = preferencesManager.themePreset
    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode

    private val _isProcessingOcr = MutableStateFlow(false)
    val isProcessingOcr: StateFlow<Boolean> = _isProcessingOcr.asStateFlow()

    private val _lastOcrResult = MutableStateFlow<ReceiptScanResult?>(null)
    val lastOcrResult: StateFlow<ReceiptScanResult?> = _lastOcrResult.asStateFlow()

    val uiState: StateFlow<ExpenseUiState>

    init {
        val database = AppDatabase.getInstance(application)
        repository = ExpenseRepository(database.expenseDao())

        allExpenses = repository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        uiState = combine(allExpenses, monthlyBudget, _isProcessingOcr, _lastOcrResult) { expenses, budget, isOcr, ocrRes ->
            calculateUiState(expenses, budget, isOcr, ocrRes)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExpenseUiState()
        )

        // Seed initial sample expenses for realistic first launch experience if empty
        viewModelScope.launch {
            checkAndSeedInitialData()
        }
    }

    private fun calculateUiState(
        expenses: List<ExpenseEntity>,
        budget: Double,
        isOcr: Boolean,
        ocrRes: ReceiptScanResult?
    ): ExpenseUiState {
        val now = Calendar.getInstance()

        // Today start timestamp
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = todayCal.timeInMillis

        // Month start timestamp
        val monthCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStart = monthCal.timeInMillis

        // 7 days ago timestamp
        val weekCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = weekCal.timeInMillis

        val todayExpenses = expenses.filter { it.timestamp >= todayStart }
        val todayTotal = todayExpenses.sumOf { it.amount }

        val todayCategoryTotals = todayExpenses.groupBy { it.category }
            .mapValues { it.value.sumOf { exp -> exp.amount } }
        val topTodayCategoryEntry = todayCategoryTotals.maxByOrNull { it.value }

        val weekExpenses = expenses.filter { it.timestamp >= weekStart }
        val weekTotal = weekExpenses.sumOf { it.amount }

        val monthExpenses = expenses.filter { it.timestamp >= monthStart }
        val monthTotal = monthExpenses.sumOf { it.amount }

        val categoryTotalsMonth = monthExpenses.groupBy { it.category }
            .mapValues { it.value.sumOf { exp -> exp.amount } }

        val progress = if (budget > 0) (monthTotal / budget).toFloat().coerceIn(0f, 1.5f) else 0f

        // Calculate spending for last 7 days (including today)
        val dayFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))
        val last7Days = mutableListOf<DaySpend>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val startOfDay = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayTotal = expenses.filter { it.timestamp in startOfDay..endOfDay }.sumOf { it.amount }
            val label = dayFormat.format(dayCal.time).replace(".", "").replaceFirstChar { it.uppercase() }
            last7Days.add(DaySpend(dayLabel = label, amount = dayTotal, isToday = (i == 0)))
        }

        return ExpenseUiState(
            expenses = expenses,
            todayTotal = todayTotal,
            weekTotal = weekTotal,
            monthTotal = monthTotal,
            monthlyBudget = budget,
            budgetProgress = progress,
            topTodayCategory = topTodayCategoryEntry?.key,
            topTodayAmount = topTodayCategoryEntry?.value ?: 0.0,
            categoryTotalsThisMonth = categoryTotalsMonth,
            dailySpendLast7Days = last7Days,
            isProcessingOcr = isOcr,
            lastOcrResult = ocrRes
        )
    }

    private suspend fun checkAndSeedInitialData() {
        val count = repository.getExpensesSince(0).size
        if (count == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            val initial = listOf(
                ExpenseEntity(
                    title = "Almoço Executivo",
                    amount = 50.0,
                    category = ExpenseCategory.ALIMENTACAO,
                    timestamp = now - 3600000L,
                    note = "Restaurante Self-Service",
                    inputMethod = "VOICE"
                ),
                ExpenseEntity(
                    title = "Supermercado Pão de Açúcar",
                    amount = 184.20,
                    category = ExpenseCategory.SUPERMERCADO,
                    timestamp = now - (dayMs * 1) - 7200000L,
                    note = "Compras semanais",
                    inputMethod = "OCR"
                ),
                ExpenseEntity(
                    title = "Uber para o Trabalho",
                    amount = 26.50,
                    category = ExpenseCategory.TRANSPORTE,
                    timestamp = now - (dayMs * 2) - 14400000L,
                    inputMethod = "VOICE"
                ),
                ExpenseEntity(
                    title = "Cinema e Pipoca",
                    amount = 62.00,
                    category = ExpenseCategory.LAZER,
                    timestamp = now - (dayMs * 3) - 18000000L,
                    inputMethod = "MANUAL"
                ),
                ExpenseEntity(
                    title = "Conta de Luz Enel",
                    amount = 145.80,
                    category = ExpenseCategory.MORADIA,
                    timestamp = now - (dayMs * 4) - 20000000L,
                    inputMethod = "OCR"
                ),
                ExpenseEntity(
                    title = "Farmácia Drogasil",
                    amount = 48.90,
                    category = ExpenseCategory.SAUDE,
                    timestamp = now - (dayMs * 5) - 12000000L,
                    inputMethod = "MANUAL"
                )
            )
            for (item in initial) {
                repository.insert(item)
            }
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: ExpenseCategory,
        timestamp: Long = System.currentTimeMillis(),
        note: String = "",
        receiptUri: String? = null,
        inputMethod: String = "MANUAL"
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title.ifBlank { "Gasto ${category.displayName}" },
                amount = amount,
                category = category,
                timestamp = timestamp,
                note = note,
                receiptUri = receiptUri,
                inputMethod = inputMethod
            )
            repository.insert(expense)

            // Check if monthly budget alert should trigger
            val updatedMonthTotal = uiState.value.monthTotal + amount
            val budget = uiState.value.monthlyBudget
            if (budget > 0 && updatedMonthTotal >= budget * 0.8) {
                NotificationHelper.sendBudgetLimitAlert(
                    getApplication(),
                    updatedMonthTotal,
                    budget
                )
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun parseSpokenExpense(spokenText: String): ParsedExpense {
        return VoiceExpenseParser.parse(spokenText)
    }

    fun processReceiptUri(context: Context, uri: Uri, onResult: (ReceiptScanResult) -> Unit) {
        viewModelScope.launch {
            _isProcessingOcr.value = true
            val result = ReceiptOcrScanner.scanReceipt(context, uri)
            _isProcessingOcr.value = false
            result.onSuccess { scanResult ->
                _lastOcrResult.value = scanResult
                onResult(scanResult)
            }.onFailure {
                // Fallback scan result
                val fallback = ReceiptScanResult(
                    title = "Recibo Digitalizado",
                    amount = 0.0,
                    category = ExpenseCategory.OUTROS,
                    merchantName = "",
                    rawExtractedText = "Não foi possível extrair texto do recibo.",
                    confidenceNotes = "Insira o valor manualmente"
                )
                _lastOcrResult.value = fallback
                onResult(fallback)
            }
        }
    }

    fun clearLastOcrResult() {
        _lastOcrResult.value = null
    }

    fun updateMonthlyBudget(budget: Double) {
        preferencesManager.setMonthlyBudget(budget)
    }

    fun updateRemindersEnabled(enabled: Boolean) {
        preferencesManager.setRemindersEnabled(enabled)
    }

    fun updateDailyReminderTime(timeStr: String) {
        preferencesManager.setDailyReminderTime(timeStr)
    }

    /**
     * Directly triggers the user-requested test notification:
     * "Você gastou R$50,00 hoje em comida?" (or actual today's numbers)
     */
    fun triggerTestExpenseReminder() {
        val state = uiState.value
        val topCategory = state.topTodayCategory ?: ExpenseCategory.ALIMENTACAO
        val topAmount = if (state.topTodayAmount > 0) state.topTodayAmount else 50.0
        val totalToday = if (state.todayTotal > 0) state.todayTotal else 50.0

        NotificationHelper.sendDailyExpenseReminder(
            getApplication(),
            topCategory = topCategory,
            topCategoryAmount = topAmount,
            totalSpentToday = totalToday
        )
    }

    /**
     * Triggers the monthly budget alert notification
     */
    fun triggerTestBudgetAlert() {
        val state = uiState.value
        NotificationHelper.sendBudgetLimitAlert(
            getApplication(),
            state.monthTotal,
            state.monthlyBudget
        )
    }

    fun shareWeeklyReportWhatsApp(context: Context) {
        val summary = WeeklyReportExporter.calculateWeeklySummary(uiState.value.expenses)
        WeeklyReportExporter.shareViaWhatsApp(context, summary)
    }

    fun shareWeeklyReportPdf(context: Context) {
        val summary = WeeklyReportExporter.calculateWeeklySummary(uiState.value.expenses)
        WeeklyReportExporter.generateAndSharePdf(context, summary)
    }

    fun setThemePreset(preset: AppThemePreset) {
        preferencesManager.setThemePreset(preset)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }
}
