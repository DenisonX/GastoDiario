package com.example

import com.example.domain.ExpenseCategory
import com.example.domain.ExpenseCategorizer
import com.example.domain.ReceiptOcrScanner
import com.example.domain.VoiceExpenseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testExpenseCategorizer() {
    assertEquals(ExpenseCategory.SUPERMERCADO, ExpenseCategorizer.categorize("Supermercado Carrefour"))
    assertEquals(ExpenseCategory.SUPERMERCADO, ExpenseCategorizer.categorize("Pão de Açúcar feira"))
    assertEquals(ExpenseCategory.TRANSPORTE, ExpenseCategorizer.categorize("Uber viagem centro"))
    assertEquals(ExpenseCategory.TRANSPORTE, ExpenseCategorizer.categorize("Gasolina Posto Shell"))
    assertEquals(ExpenseCategory.LAZER, ExpenseCategorizer.categorize("Cinema Cinemark ingresso"))
    assertEquals(ExpenseCategory.ALIMENTACAO, ExpenseCategorizer.categorize("Almoço restaurante a quilo"))
    assertEquals(ExpenseCategory.MORADIA, ExpenseCategorizer.categorize("Conta de luz Enel"))
    assertEquals(ExpenseCategory.SAUDE, ExpenseCategorizer.categorize("Farmácia Drogasil remédio"))
  }

  @Test
  fun testVoiceExpenseParser() {
    val result1 = VoiceExpenseParser.parse("Gastei 50 reais no almoço")
    assertEquals(50.0, result1.amount, 0.01)
    assertEquals(ExpenseCategory.ALIMENTACAO, result1.category)

    val result2 = VoiceExpenseParser.parse("Uber 25 reais")
    assertEquals(25.0, result2.amount, 0.01)
    assertEquals(ExpenseCategory.TRANSPORTE, result2.category)

    val result3 = VoiceExpenseParser.parse("Supermercado 142,50")
    assertEquals(142.50, result3.amount, 0.01)
    assertEquals(ExpenseCategory.SUPERMERCADO, result3.category)
  }

  @Test
  fun testReceiptOcrParsing() {
    val sampleReceipt = """
      SUPERMERCADO DIA BRASIL
      AV PAULISTA 1234
      LEITE INTEGRAL R$ 5,90
      ARROZ BRANCO R$ 24,90
      SUBTOTAL R$ 30,80
      TOTAL A PAGAR R$ 30,80
      FORMA DE PAGAMENTO: CARTAO
    """.trimIndent()

    val parsed = ReceiptOcrScanner.parseReceiptText(sampleReceipt)
    assertEquals(30.80, parsed.amount, 0.01)
    assertEquals(ExpenseCategory.SUPERMERCADO, parsed.category)
    assertTrue(parsed.merchantName.contains("SUPERMERCADO", ignoreCase = true))
  }

  @Test
  fun testExpenseEntityWithDateAndCategory() {
    val timestamp = 1773000000000L
    val expense = com.example.data.ExpenseEntity(
        title = "Supermercado Extra",
        amount = 129.50,
        category = ExpenseCategory.SUPERMERCADO,
        timestamp = timestamp,
        note = "Compras do mês",
        inputMethod = "MANUAL"
    )
    assertEquals("Supermercado Extra", expense.title)
    assertEquals(129.50, expense.amount, 0.001)
    assertEquals(ExpenseCategory.SUPERMERCADO, expense.category)
    assertEquals(timestamp, expense.timestamp)
    assertEquals("Compras do mês", expense.note)
    assertEquals("MANUAL", expense.inputMethod)
  }

  @Test
  fun testThemePresetsAvailability() {
    val presets = com.example.ui.theme.AppThemePreset.entries
    assertTrue(presets.size >= 7)
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.EMERALD))
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.OCEAN_BLUE))
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.AMETHYST_PURPLE))
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.SUNSET_GOLD))
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.BERRY_ROSE))
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.GRAPHITE_DARK))
    assertTrue(presets.contains(com.example.ui.theme.AppThemePreset.FOREST_PINE))

    // Ensure all presets have non-null color schemes
    presets.forEach { preset ->
      org.junit.Assert.assertNotNull(preset.lightColorScheme)
      org.junit.Assert.assertNotNull(preset.darkColorScheme)
      assertTrue(preset.title.isNotBlank())
    }
  }

  @Test
  fun testThemeModes() {
    val modes = com.example.ui.theme.ThemeMode.entries
    assertEquals(3, modes.size)
    assertTrue(modes.contains(com.example.ui.theme.ThemeMode.SYSTEM))
    assertTrue(modes.contains(com.example.ui.theme.ThemeMode.LIGHT))
    assertTrue(modes.contains(com.example.ui.theme.ThemeMode.DARK))
  }
}
