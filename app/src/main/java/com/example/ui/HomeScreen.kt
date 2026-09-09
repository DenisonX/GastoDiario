package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ExpenseEntity
import com.example.domain.ExpenseCategory
import com.example.domain.NotificationHelper
import com.example.ui.components.AddExpenseBottomSheet
import com.example.ui.components.BudgetSettingsDialog
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailySpendBarChart
import com.example.ui.components.ExpenseItemRow
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.ReceiptOcrDialog
import com.example.ui.components.ThemeSelectorDialog
import com.example.ui.components.VoiceInputDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val isProcessingOcr by viewModel.isProcessingOcr.collectAsStateWithLifecycle()
    val currentPreset by viewModel.themePreset.collectAsStateWithLifecycle()
    val currentMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val ptBr = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val scope = rememberCoroutineScope()
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dialog & Sheet States
    var showThemeDialog by remember { mutableStateOf(false) }
    var showExpenseEntryScreen by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showOcrDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    // Tab state: 0 = Visão Geral & Gráficos, 1 = Histórico Detalhado
    var selectedTab by remember { mutableIntStateOf(0) }
    var categoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }

    // Pre-fill state when coming from Voice or OCR
    var prefillTitle by remember { mutableStateOf("") }
    var prefillAmount by remember { mutableStateOf(0.0) }
    var prefillCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var prefillMethod by remember { mutableStateOf("MANUAL") }

    // Runtime permission for POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Granted or denied */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        NotificationHelper.createNotificationChannel(context)
    }

    // Render full-screen Expense Entry Screen if requested
    if (showExpenseEntryScreen) {
        ExpenseEntryScreen(
            onNavigateBack = { showExpenseEntryScreen = false },
            onSaveExpense = { title, amount, category, timestamp, note, method ->
                viewModel.addExpense(
                    title = title,
                    amount = amount,
                    category = category,
                    timestamp = timestamp,
                    note = note,
                    inputMethod = method
                )
            },
            onOpenVoiceInput = {
                showExpenseEntryScreen = false
                showVoiceDialog = true
            },
            onOpenOcrReceipt = {
                showExpenseEntryScreen = false
                showOcrDialog = true
            },
            initialTitle = prefillTitle,
            initialAmount = prefillAmount,
            initialCategory = prefillCategory,
            initialMethod = prefillMethod,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    prefillTitle = ""
                    prefillAmount = 0.0
                    prefillCategory = null
                    prefillMethod = "MANUAL"
                    showExpenseEntryScreen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("fab_add_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Gasto",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // App Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("app_logo_image")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo_1788917615516),
                            contentDescription = "Logo GastoDiário",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column {
                        Text(
                            text = "GastoDiário",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Controle financeiro pessoal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Theme customization button
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("top_bar_theme_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Personalizar Tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Export weekly report button
                    IconButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("top_bar_export_report_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Relatórios Semanais",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Budget settings & alerts button
                    IconButton(
                        onClick = { showBudgetDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("top_bar_budget_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Lembretes e Limites",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Main Financial Summary Card
            val progressColor by animateColorAsState(
                targetValue = when {
                    uiState.budgetProgress >= 1f -> Color(0xFFD32F2F) // Red
                    uiState.budgetProgress >= 0.8f -> Color(0xFFFF8B00) // Amber
                    else -> MaterialTheme.colorScheme.primary
                },
                label = "progressColor"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("main_budget_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Gasto do Mês",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ptBr.format(uiState.monthTotal),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Today's Expense Pill
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Hoje",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ptBr.format(uiState.todayTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Budget Progress Bar
                    LinearProgressIndicator(
                        progress = { uiState.budgetProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pct = (uiState.budgetProgress * 100).toInt()
                        Text(
                            text = "$pct% do teto mensal (${ptBr.format(uiState.monthlyBudget)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = progressColor,
                            fontWeight = FontWeight.SemiBold
                        )

                        val remaining = uiState.monthlyBudget - uiState.monthTotal
                        Text(
                            text = if (remaining >= 0) "Restante: ${ptBr.format(remaining)}" else "Excedido: ${ptBr.format(-remaining)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Budget limit warning badge if >= 80%
                    if (uiState.budgetProgress >= 0.8f) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(progressColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = progressColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.budgetProgress >= 1f) "Limite mensal ultrapassado! Controle seus próximos gastos."
                                else "Atenção: Você atingiu 80% do limite mensal estipulado.",
                                style = MaterialTheme.typography.labelSmall,
                                color = progressColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Banner for Expense Entry Screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        prefillTitle = ""
                        prefillAmount = 0.0
                        prefillCategory = null
                        prefillMethod = "MANUAL"
                        showExpenseEntryScreen = true
                    }
                    .testTag("action_new_expense_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Novo Lançamento de Gasto",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Valor, categoria e data com ícones Material",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Abrir tela de entrada",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Input Action Bar (Voz, Recibo OCR, Relatório)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voice Input Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { showVoiceDialog = true }
                        .testTag("action_voice_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Entrada por Voz",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Gasto por Voz",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Dizer gasto",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // OCR Receipt Scanner Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { showOcrDialog = true }
                        .testTag("action_ocr_receipt"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ler Recibo OCR",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Foto Recibo",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "OCR Gratuito",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Navigation (Gráficos vs Lista de Lançamentos)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Gráficos & Categorias",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Lançamentos (${uiState.expenses.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Tab Content
            when (selectedTab) {
                0 -> {
                    // Gráficos e Categorias Automáticas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            CategoryDonutChart(
                                categoryTotals = uiState.categoryTotalsThisMonth,
                                totalAmount = uiState.monthTotal
                            )
                        }

                        item {
                            DailySpendBarChart(
                                days = uiState.dailySpendLast7Days
                            )
                        }

                        item {
                            // Quick tip card about inflation and variable accounts in Brazil
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💡",
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Dica para Inflação no Brasil:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Anotar pequenos gastos diários por voz evita desvios invisíveis no orçamento e mantém seu poder de compra.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Histórico Detalhado dos Gastos
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Category filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (categoryFilter == null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { categoryFilter = null }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Todos",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (categoryFilter == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            ExpenseCategory.entries.forEach { cat ->
                                val isSelected = categoryFilter == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) cat.color
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { categoryFilter = if (isSelected) null else cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredList = uiState.expenses.filter {
                            categoryFilter == null || it.category == categoryFilter
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📝", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Nenhum gasto encontrado",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Use os botões de voz ou recibo para adicionar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(filteredList, key = { it.id }) { expense ->
                                    ExpenseItemRow(
                                        expense = expense,
                                        onDelete = { viewModel.deleteExpense(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add Expense
    if (showAddSheet) {
        AddExpenseBottomSheet(
            sheetState = addSheetState,
            onDismiss = { showAddSheet = false },
            onSave = { title, amount, category, timestamp, note, method ->
                viewModel.addExpense(
                    title = title,
                    amount = amount,
                    category = category,
                    timestamp = timestamp,
                    note = note,
                    inputMethod = method
                )
            },
            onOpenVoice = {
                showAddSheet = false
                showVoiceDialog = true
            },
            onOpenOcr = {
                showAddSheet = false
                showOcrDialog = true
            },
            initialTitle = prefillTitle,
            initialAmount = prefillAmount,
            initialCategory = prefillCategory,
            inputMethod = prefillMethod
        )
    }

    // Voice Input Recognition Dialog
    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onExpenseConfirmed = { title, amount, category, method ->
                viewModel.addExpense(
                    title = title,
                    amount = amount,
                    category = category,
                    inputMethod = method
                )
            }
        )
    }

    // Receipt OCR Dialog (Free on-device ML Kit)
    if (showOcrDialog) {
        ReceiptOcrDialog(
            isProcessing = isProcessingOcr,
            onDismiss = { showOcrDialog = false },
            onSelectImageUri = { uri, onResult ->
                viewModel.processReceiptUri(context, uri, onResult)
            },
            onExpenseConfirmed = { title, amount, category, note, method ->
                viewModel.addExpense(
                    title = title,
                    amount = amount,
                    category = category,
                    note = note,
                    inputMethod = method
                )
            }
        )
    }

    // Weekly Export Dialog (WhatsApp & PDF)
    if (showReportDialog) {
        ExportReportDialog(
            expenses = uiState.expenses,
            onDismiss = { showReportDialog = false },
            onShareWhatsApp = {
                viewModel.shareWeeklyReportWhatsApp(context)
            },
            onSharePdf = {
                viewModel.shareWeeklyReportPdf(context)
            }
        )
    }

    // Budget & Notification Settings Dialog
    if (showBudgetDialog) {
        BudgetSettingsDialog(
            currentBudget = uiState.monthlyBudget,
            remindersEnabled = remindersEnabled,
            onDismiss = { showBudgetDialog = false },
            onSaveBudget = { newBudget ->
                viewModel.updateMonthlyBudget(newBudget)
            },
            onToggleReminders = { enabled ->
                viewModel.updateRemindersEnabled(enabled)
            },
            onTestDailyReminder = {
                viewModel.triggerTestExpenseReminder()
            },
            onTestBudgetAlert = {
                viewModel.triggerTestBudgetAlert()
            }
        )
    }

    // Theme Customization Dialog
    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentPreset = currentPreset,
            currentMode = currentMode,
            onPresetSelected = { viewModel.setThemePreset(it) },
            onModeSelected = { viewModel.setThemeMode(it) },
            onDismiss = { showThemeDialog = false }
        )
    }
}
