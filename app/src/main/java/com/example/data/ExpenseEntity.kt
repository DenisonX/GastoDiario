package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.ExpenseCategory

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val receiptUri: String? = null,
    val inputMethod: String = "MANUAL" // "VOICE", "OCR", "MANUAL"
)
