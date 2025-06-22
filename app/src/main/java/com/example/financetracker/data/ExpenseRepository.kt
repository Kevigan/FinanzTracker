package com.example.financetracker.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) {
    fun getExpenses(): Flow<List<Expense>> = dao.getAllExpenses()

    suspend fun addExpense(expense: Expense) {
        dao.insertExpense(expense)
    }
}
