package com.example.domain

data class ParsedExpense(
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val rawText: String
)

object VoiceExpenseParser {

    /**
     * Parses spoken Portuguese text into an expense.
     * Handles sentences like:
     * - "Gastei 50 reais no almoço"
     * - "35 e 90 de Uber"
     * - "Supermercado 142 reais e 50 centavos"
     * - "Almoço 35"
     */
    fun parse(spokenText: String): ParsedExpense {
        val trimmed = spokenText.trim()
        val lower = trimmed.lowercase()

        // 1. Try to find numerical amount
        // Handle "X reais e Y centavos" or "X e Y" or "R$ X,YY" or "X,YY" or "X.YY" or pure number "X"
        var amount = 0.0
        var matchedNumberStr = ""

        // Pattern: "R$ 45,90" or "45,90" or "45.90"
        val priceRegex = Regex("""(?:r\$\s*)?(\d+)[,\.](\d{1,2})""", RegexOption.IGNORE_CASE)
        val priceMatch = priceRegex.find(lower)

        if (priceMatch != null) {
            val intPart = priceMatch.groupValues[1]
            val decPart = priceMatch.groupValues[2].padEnd(2, '0').take(2)
            amount = "$intPart.$decPart".toDoubleOrNull() ?: 0.0
            matchedNumberStr = priceMatch.value
        } else {
            // Pattern: "45 reais e 50 centavos" or "45 reais e 50"
            val verbalCentsRegex = Regex("""(\d+)\s*(?:reais)?\s*e\s*(\d{1,2})\s*(?:centavos)?""", RegexOption.IGNORE_CASE)
            val verbalMatch = verbalCentsRegex.find(lower)
            if (verbalMatch != null) {
                val intPart = verbalMatch.groupValues[1]
                val decPart = verbalMatch.groupValues[2].padEnd(2, '0').take(2)
                amount = "$intPart.$decPart".toDoubleOrNull() ?: 0.0
                matchedNumberStr = verbalMatch.value
            } else {
                // Pattern: simple integer: "50 reais" or just "50"
                val simpleReaisRegex = Regex("""(\d+)\s*(?:reais)?""", RegexOption.IGNORE_CASE)
                val simpleMatch = simpleReaisRegex.find(lower)
                if (simpleMatch != null) {
                    amount = simpleMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                    matchedNumberStr = simpleMatch.value
                }
            }
        }

        // Clean description by removing the amount part and filler words
        var cleaned = lower
        if (matchedNumberStr.isNotEmpty()) {
            cleaned = cleaned.replace(matchedNumberStr, "")
        }

        // Remove typical fillers: "gastei", "comprei", "paguei", "reais", "no", "na", "de", "com", "em"
        val fillers = listOf("gastei", "comprei", "paguei", "valor", "reais", "centavos", "custou", "hoje", "ontem")
        for (filler in fillers) {
            cleaned = cleaned.replace(Regex("""\b$filler\b""", RegexOption.IGNORE_CASE), " ")
        }

        // Clean prepositions at start/end
        cleaned = cleaned.trim().replace(Regex("""^(no|na|de|do|da|em|com|para|pro|pra)\s+""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+(no|na|de|do|da|em|com|para|pro|pra)$""", RegexOption.IGNORE_CASE), "")
            .trim()

        val title = if (cleaned.isNotEmpty()) {
            cleaned.replaceFirstChar { it.uppercase() }
        } else {
            "Gasto Registrado"
        }

        val category = ExpenseCategorizer.categorize("$title $spokenText")

        return ParsedExpense(
            title = title,
            amount = amount,
            category = category,
            rawText = spokenText
        )
    }
}
