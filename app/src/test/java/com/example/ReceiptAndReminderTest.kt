package com.example

import com.example.domain.DailyReminderScheduler
import com.example.domain.ReceiptOcrScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReceiptAndReminderTest {

  @Test
  fun parseAmountHandlesBrazilianAndInternationalFormats() {
    assertEquals(12.34, ReceiptOcrScanner.parseAmount("12,34")!!, 0.001)
    assertEquals(12.34, ReceiptOcrScanner.parseAmount("12.34")!!, 0.001)
    assertEquals(1234.56, ReceiptOcrScanner.parseAmount("1.234,56")!!, 0.001)
    assertEquals(1234.56, ReceiptOcrScanner.parseAmount("1,234.56")!!, 0.001)
    assertEquals(12345678.90, ReceiptOcrScanner.parseAmount("12.345.678,90")!!, 0.001)
    assertNull(ReceiptOcrScanner.parseAmount("12"))
  }

  @Test
  fun receiptWithDotDecimalIsNotMultipliedByHundred() {
    val receipt = """
      PADARIA CENTRAL
      PAO FRANCES 8.50
      TOTAL 12.34
    """.trimIndent()
    assertEquals(12.34, ReceiptOcrScanner.parseReceiptText(receipt).amount, 0.001)
  }

  @Test
  fun receiptWithThousandsSeparatorIsParsedFully() {
    val receipt = """
      LOJA ELETRONICOS
      DATA 12.03.2026
      TELEVISAO R$ 1.234,56
      VALOR TOTAL R$ 1.234,56
    """.trimIndent()
    assertEquals(1234.56, ReceiptOcrScanner.parseReceiptText(receipt).amount, 0.001)
  }

  @Test
  fun parseTimeFallsBackToDefaultOnInvalidInput() {
    assertEquals(8 to 5, DailyReminderScheduler.parseTime("08:05"))
    assertEquals(20 to 30, DailyReminderScheduler.parseTime("25:99"))
    assertEquals(20 to 30, DailyReminderScheduler.parseTime("abc"))
  }

  @Test
  fun nextTriggerIsTodayWhenTimeHasNotPassedAndTomorrowOtherwise() {
    val tz = TimeZone.getTimeZone("America/Sao_Paulo")
    val now = Calendar.getInstance(tz).apply {
      set(2026, Calendar.SEPTEMBER, 29, 18, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val later = Calendar.getInstance(tz).apply {
      timeInMillis = DailyReminderScheduler.nextTriggerMillis(now, 20, 30, tz)
    }
    assertEquals(29, later.get(Calendar.DAY_OF_MONTH))
    assertEquals(20, later.get(Calendar.HOUR_OF_DAY))
    assertEquals(30, later.get(Calendar.MINUTE))

    val earlier = Calendar.getInstance(tz).apply {
      timeInMillis = DailyReminderScheduler.nextTriggerMillis(now, 7, 0, tz)
    }
    assertEquals(30, earlier.get(Calendar.DAY_OF_MONTH))
    assertEquals(7, earlier.get(Calendar.HOUR_OF_DAY))
  }
}
