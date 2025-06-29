package com.example.financetracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val incomeDao: IncomeDao
) {

    //  Expenses
    fun getExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    //  Budget
    fun getBudget(): Flow<Budget?> = budgetDao.getBudget()

    suspend fun setBudget(
        amount: Double,
        period: BudgetPeriod,
        id: Int = 0 // optional override
    ) {
        val budget = Budget(id = id, amount = amount, period = period)
        budgetDao.setBudget(budget)
    }

    suspend fun updateBudget(budget: Budget){budgetDao.updateBudget(budget)}

    // Incomes
    fun getIncomes(): Flow<List<Income>> = incomeDao.getAllIncomes()

    suspend fun addIncome(income: Income) {
        incomeDao.insertIncome(income)
    }

    fun getAllTransactions(): Flow<List<Transaction>> =
        combine(
            getExpenses(),
            getIncomes(),
            getBudget()
        ) { expenses, incomes, budgetOpt ->

            val expenseTxs = expenses.map { Transaction.ExpenseTransaction(it) }
            val incomeTxs = incomes.map { Transaction.IncomeTransaction(it) }
            val budgetTxs = budgetOpt?.let { listOf(Transaction.BudgetTransaction(it)) } ?: emptyList()

            (expenseTxs + incomeTxs + budgetTxs).sortedByDescending { it.createdAt }
        }

    // --- Expense update/delete ---
    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    // --- Income update/delete ---
    suspend fun updateIncome(income: Income) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: Income) {
        incomeDao.deleteIncome(income)
    }

}


