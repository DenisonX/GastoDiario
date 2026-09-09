package com.example.domain

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.ExpenseEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeeklyReportExporter {

    private val ptBrFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR"))

    data class WeeklySummary(
        val startDate: Date,
        val endDate: Date,
        val totalSpent: Double,
        val dailyAverage: Double,
        val categoryTotals: Map<ExpenseCategory, Double>,
        val categoryPercentages: Map<ExpenseCategory, Double>,
        val expenses: List<ExpenseEntity>
    )

    fun calculateWeeklySummary(allExpenses: List<ExpenseEntity>): WeeklySummary {
        val now = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val weekExpenses = allExpenses.filter { it.timestamp >= startCal.timeInMillis }
        val totalSpent = weekExpenses.sumOf { it.amount }
        val dailyAverage = totalSpent / 7.0

        val categoryTotals = weekExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val categoryPercentages = if (totalSpent > 0) {
            categoryTotals.mapValues { (it.value / totalSpent) * 100.0 }
        } else {
            emptyMap()
        }

        return WeeklySummary(
            startDate = startCal.time,
            endDate = endCal.time,
            totalSpent = totalSpent,
            dailyAverage = dailyAverage,
            categoryTotals = categoryTotals,
            categoryPercentages = categoryPercentages,
            expenses = weekExpenses.sortedByDescending { it.timestamp }
        )
    }

    /**
     * Generates a beautifully formatted WhatsApp text message
     */
    fun buildWhatsAppReportText(summary: WeeklySummary): String {
        val sb = StringBuilder()
        sb.append("📊 *RELATÓRIO SEMANAL DE GASTOS* 📊\n")
        sb.append("🗓️ *Período:* ${dateFormat.format(summary.startDate)} até ${dateFormat.format(summary.endDate)}\n\n")

        sb.append("💰 *Total Gasto na Semana:* ${ptBrFormat.format(summary.totalSpent)}\n")
        sb.append("📈 *Média Diária:* ${ptBrFormat.format(summary.dailyAverage)}\n")
        sb.append("🧾 *Total de Registros:* ${summary.expenses.size}\n\n")

        sb.append("🏷️ *Divisão por Categorias:*\n")
        if (summary.categoryTotals.isEmpty()) {
            sb.append("• Nenhum gasto registrado no período.\n")
        } else {
            summary.categoryTotals.entries.sortedByDescending { it.value }.forEach { (cat, amount) ->
                val pct = summary.categoryPercentages[cat] ?: 0.0
                val icon = when (cat) {
                    ExpenseCategory.SUPERMERCADO -> "🛒"
                    ExpenseCategory.TRANSPORTE -> "🚗"
                    ExpenseCategory.LAZER -> "🎮"
                    ExpenseCategory.ALIMENTACAO -> "🍔"
                    ExpenseCategory.MORADIA -> "🏠"
                    ExpenseCategory.SAUDE -> "💊"
                    ExpenseCategory.OUTROS -> "📦"
                }
                sb.append("$icon *${cat.displayName}:* ${ptBrFormat.format(amount)} (${String.format(Locale.ROOT, "%.1f", pct)}%)\n")
            }
        }

        if (summary.expenses.isNotEmpty()) {
            sb.append("\n🔝 *Principais Lançamentos:*\n")
            summary.expenses.take(5).forEach { exp ->
                sb.append("• ${ptBrFormat.format(exp.amount)} - ${exp.title} (${dateFormat.format(Date(exp.timestamp))})\n")
            }
        }

        sb.append("\n_Gerado pelo GastoDiário • Controle Financeiro Inteligente_ 🇧🇷")
        return sb.toString()
    }

    /**
     * Sends the formatted report directly to WhatsApp (with fallback)
     */
    fun shareViaWhatsApp(context: Context, summary: WeeklySummary) {
        val text = buildWhatsAppReportText(summary)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // Fallback: WhatsApp not installed, open standard chooser
            val chooser = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "Compartilhar Relatório Financeiro"
            )
            context.startActivity(chooser)
        }
    }

    /**
     * Creates and shares a real PDF report using Android PdfDocument
     */
    fun generateAndSharePdf(context: Context, summary: WeeklySummary) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.rgb(0, 135, 90) // Emerald Green
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = AndroidColor.rgb(94, 108, 132)
            textSize = 11f
            isAntiAlias = true
        }

        val headerBoxPaint = Paint().apply {
            color = AndroidColor.rgb(227, 252, 239)
            style = Paint.Style.FILL
        }

        val textBold = Paint().apply {
            color = AndroidColor.rgb(23, 43, 77)
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textRegular = Paint().apply {
            color = AndroidColor.rgb(40, 50, 60)
            textSize = 11f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = AndroidColor.rgb(223, 225, 230)
            strokeWidth = 1f
        }

        var y = 45f

        // Title
        canvas.drawText("GASTODIÁRIO • RELATÓRIO SEMANAL", 40f, y, titlePaint)
        y += 18f
        val periodText = "Período: ${dateFormat.format(summary.startDate)} a ${dateFormat.format(summary.endDate)} • Emitido em: ${dateFormat.format(Date())}"
        canvas.drawText(periodText, 40f, y, subtitlePaint)
        y += 20f

        // Top Summary Card (Total & Average)
        canvas.drawRoundRect(40f, y, 555f, y + 70f, 10f, 10f, headerBoxPaint)
        canvas.drawText("TOTAL GASTO NA SEMANA", 60f, y + 28f, subtitlePaint)
        canvas.drawText(ptBrFormat.format(summary.totalSpent), 60f, y + 54f, titlePaint)

        canvas.drawText("MÉDIA DIÁRIA", 320f, y + 28f, subtitlePaint)
        val avgPaint = Paint(textBold).apply { textSize = 16f }
        canvas.drawText(ptBrFormat.format(summary.dailyAverage), 320f, y + 54f, avgPaint)
        y += 95f

        // Categories Breakdown Section
        canvas.drawText("Distribuição por Categorias", 40f, y, textBold)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        if (summary.categoryTotals.isEmpty()) {
            canvas.drawText("Nenhuma despesa registrada nos últimos 7 dias.", 40f, y, textRegular)
            y += 25f
        } else {
            summary.categoryTotals.entries.sortedByDescending { it.value }.forEach { (cat, amount) ->
                val pct = summary.categoryPercentages[cat] ?: 0.0
                val catText = "${cat.displayName}: ${ptBrFormat.format(amount)} (${String.format(Locale.ROOT, "%.1f", pct)}%)"
                canvas.drawText(catText, 45f, y, textRegular)

                // Category bar representation
                val barWidth = ((pct / 100.0) * 180).toFloat().coerceIn(4f, 180f)
                val barPaint = Paint().apply {
                    color = when (cat) {
                        ExpenseCategory.SUPERMERCADO -> AndroidColor.rgb(54, 179, 126)
                        ExpenseCategory.TRANSPORTE -> AndroidColor.rgb(0, 101, 255)
                        ExpenseCategory.LAZER -> AndroidColor.rgb(255, 139, 0)
                        ExpenseCategory.ALIMENTACAO -> AndroidColor.rgb(255, 86, 48)
                        ExpenseCategory.MORADIA -> AndroidColor.rgb(101, 84, 192)
                        ExpenseCategory.SAUDE -> AndroidColor.rgb(0, 184, 217)
                        ExpenseCategory.OUTROS -> AndroidColor.rgb(122, 134, 154)
                    }
                }
                canvas.drawRoundRect(350f, y - 10f, 350f + barWidth, y - 2f, 4f, 4f, barPaint)
                y += 20f
            }
        }
        y += 15f

        // Detailed Itemized List
        canvas.drawText("Detalhamento dos Lançamentos da Semana", 40f, y, textBold)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        val tableHeaderPaint = Paint(subtitlePaint).apply { isFakeBoldText = true }
        canvas.drawText("Data/Hora", 45f, y, tableHeaderPaint)
        canvas.drawText("Descrição", 140f, y, tableHeaderPaint)
        canvas.drawText("Categoria", 340f, y, tableHeaderPaint)
        canvas.drawText("Valor", 480f, y, tableHeaderPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 16f

        val maxList = summary.expenses.take(22)
        for (item in maxList) {
            canvas.drawText(timeFormat.format(Date(item.timestamp)), 45f, y, textRegular)
            val titleTrimmed = if (item.title.length > 25) item.title.take(23) + "..." else item.title
            canvas.drawText(titleTrimmed, 140f, y, textRegular)
            canvas.drawText(item.category.displayName, 340f, y, textRegular)
            canvas.drawText(ptBrFormat.format(item.amount), 480f, y, textBold)
            y += 18f
        }

        if (summary.expenses.size > 22) {
            val remaining = summary.expenses.size - 22
            canvas.drawText("... e mais $remaining outros gastos.", 45f, y, subtitlePaint)
            y += 18f
        }

        // Footer
        canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
        canvas.drawText("GastoDiário • Controle financeiro pessoal minimalista para o Brasil", 40f, 815f, subtitlePaint)

        document.finishPage(page)

        // Save PDF to cache dir
        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val pdfFile = File(reportsDir, "relatorio_gastos_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            document.close()
            outputStream.flush()
            outputStream.close()

            // Open share intent
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório Semanal de Gastos")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório PDF"))
        } catch (e: Exception) {
            document.close()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
