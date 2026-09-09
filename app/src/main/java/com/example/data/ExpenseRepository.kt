package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesBetween(startTime, endTime)
    }

    suspend fun insert(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun update(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun delete(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteById(id: Long) {
        expenseDao.deleteById(id)
    }

    suspend fun getExpensesSince(sinceTimestamp: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesSince(sinceTimestamp)
    }
}
