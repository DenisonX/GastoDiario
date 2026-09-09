package com.example.domain

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_ID = "gastodiario_reminders"
    private const val NOTIFICATION_ID_DAILY = 1001
    private const val NOTIFICATION_ID_BUDGET = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes & Alertas Financeiros"
            val descriptionText = "Lembretes diários de controle de gastos e alertas de limite de orçamento"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sends the prompt's specific reminder:
     * "Você gastou R$50,00 hoje em comida?" or personalized to the highest spent category today.
     */
    fun sendDailyExpenseReminder(
        context: Context,
        topCategory: ExpenseCategory? = ExpenseCategory.ALIMENTACAO,
        topCategoryAmount: Double = 50.0,
        totalSpentToday: Double = 50.0
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val ptBrFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val formattedAmount = ptBrFormat.format(topCategoryAmount)
        val categoryName = topCategory?.displayName?.lowercase() ?: "comida"

        val title = "Lembrete de Controle Diário"
        val message = if (topCategoryAmount > 0) {
            "Você gastou $formattedAmount hoje em $categoryName. Registrou todos os seus gastos de hoje?"
        } else {
            "Você ainda não registrou nenhum gasto hoje. Que tal anotar para manter o controle?"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, builder.build())
    }

    /**
     * Sends the monthly limit alert:
     * Warns if spending approaches 80% or exceeds 100% of the budget.
     */
    fun sendBudgetLimitAlert(
        context: Context,
        monthlySpent: Double,
        monthlyBudget: Double
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val ptBr = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val percent = if (monthlyBudget > 0) (monthlySpent / monthlyBudget) * 100 else 0.0

        val (title, message) = when {
            percent >= 100 -> {
                "🚨 Limite Mensal Ultrapassado!" to
                        "Atenção: Você gastou ${ptBr.format(monthlySpent)} do seu limite de ${ptBr.format(monthlyBudget)} (${String.format(Locale.ROOT, "%.0f", percent)}%). É hora de conter despesas!"
            }
            percent >= 80 -> {
                "⚠️ Alerta de Limite Mensal (80%)" to
                        "Cuidado: Você já atingiu ${String.format(Locale.ROOT, "%.0f", percent)}% do seu orçamento mensal (${ptBr.format(monthlySpent)} de ${ptBr.format(monthlyBudget)})."
            }
            else -> {
                "✅ Orçamento sob controle" to
                        "Você gastou ${ptBr.format(monthlySpent)} de ${ptBr.format(monthlyBudget)} este mês (${String.format(Locale.ROOT, "%.0f", percent)}%). Continue assim!"
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BUDGET, builder.build())
    }
}
