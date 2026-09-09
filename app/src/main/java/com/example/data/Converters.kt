package com.example.data

import androidx.room.TypeConverter
import com.example.domain.ExpenseCategory

class Converters {
    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String {
        return value.name
    }

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory {
        return ExpenseCategory.fromString(value)
    }
}
