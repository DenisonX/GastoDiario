package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

data class ReceiptScanResult(
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val merchantName: String,
    val rawExtractedText: String,
    val confidenceNotes: String
)

object ReceiptOcrScanner {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun scanReceipt(context: Context, imageUri: Uri): Result<ReceiptScanResult> {
        return try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            processImage(inputImage)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanReceiptBitmap(bitmap: Bitmap): Result<ReceiptScanResult> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            processImage(inputImage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processImage(image: InputImage): Result<ReceiptScanResult> =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    val parsed = parseReceiptText(text)
                    continuation.resume(Result.success(parsed))
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
        }

    /**
     * Matches monetary values with exactly two decimal places, with or without thousands
     * separators: 12,34 · 12.34 · 1.234,56 · 1,234.56 · 12.345.678,90.
     * The lookarounds prevent matching fragments of dates (12.03.2026) or longer numbers.
     */
    private val priceRegex =
        Regex("""(?<![\d.,])(\d{1,3}(?:[.,]\d{3})+[.,]\d{2}|\d{1,6}[.,]\d{2})(?!\d|[.,]\d)""")

    /**
     * Converts a matched price string into a Double. The separator before the last two
     * digits is always the decimal separator; every other separator is a thousands separator.
     */
    fun parseAmount(raw: String): Double? {
        val value = raw.trim()
        if (value.length < 4) return null
        val decimalSeparator = value[value.length - 3]
        if (decimalSeparator != ',' && decimalSeparator != '.') return null
        val integerPart = value.substring(0, value.length - 3).filter { it.isDigit() }
        val cents = value.takeLast(2)
        if (integerPart.isEmpty() || !cents.all { it.isDigit() }) return null
        return "$integerPart.$cents".toDoubleOrNull()
    }

    fun parseReceiptText(rawText: String): ReceiptScanResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var merchantName = ""
        var totalAmount = 0.0


        // 1. Identify Merchant Name from top 5 lines
        for (i in 0 until minOf(5, lines.size)) {
            val line = lines[i]
            val lower = line.lowercase()
            // Ignore lines that look like CNPJ, CPF, date, phone, address headers
            if (!lower.contains("cnpj") && !lower.contains("cpf") && !lower.contains("ie:") &&
                !lower.contains("data") && !lower.contains("hora") && !lower.contains("extrato") &&
                !lower.contains("cupom") && !lower.contains("sat") && !lower.contains("danfe") &&
                line.length >= 3 && !line.matches(Regex("""[\d\s\.\,\-\/]+"""))
            ) {
                merchantName = line
                break
            }
        }
        if (merchantName.isEmpty() && lines.isNotEmpty()) {
            merchantName = lines.first().take(30)
        }

        // 2. Identify Total Amount
        // Look for lines containing "TOTAL", "A PAGAR", "VALOR LIQUIDO", "DEBITO", "CREDITO", "PIX"
        val totalKeywords = listOf("total", "a pagar", "valor liquido", "valor total", "subtotal", "pago")
        var foundExplicitTotal = false

        for (line in lines) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                val matches = priceRegex.findAll(line).toList()
                if (matches.isNotEmpty()) {
                    val parsed = parseAmount(matches.last().groupValues[1])
                    if (parsed != null && parsed > 0) {
                        totalAmount = parsed
                        foundExplicitTotal = true
                        break
                    }
                }
            }
        }

        // Fallback: If no line with "TOTAL" had an amount, find the largest price on receipt
        if (!foundExplicitTotal || totalAmount <= 0.0) {
            val allAmounts = mutableListOf<Double>()
            for (line in lines) {
                for (match in priceRegex.findAll(line)) {
                    val num = parseAmount(match.groupValues[1])
                    if (num != null && num in 0.50..50000.0) {
                        allAmounts.add(num)
                    }
                }
            }
            if (allAmounts.isNotEmpty()) {
                totalAmount = allAmounts.maxOrNull() ?: 0.0
            }
        }

        val category = ExpenseCategorizer.categorize("$merchantName $rawText")
        val cleanTitle = if (merchantName.isNotBlank()) merchantName else "Recibo ${category.displayName}"

        val notes = if (foundExplicitTotal) "Total identificado automaticamente no comprovante"
        else if (totalAmount > 0) "Maior valor detectado no comprovante"
        else "Revise o valor extraído"

        return ReceiptScanResult(
            title = cleanTitle,
            amount = totalAmount,
            category = category,
            merchantName = merchantName,
            rawExtractedText = rawText,
            confidenceNotes = notes
        )
    }
}
