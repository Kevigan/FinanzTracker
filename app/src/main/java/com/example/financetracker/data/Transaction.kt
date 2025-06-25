package com.example.financetracker.data

sealed class Transaction {
    abstract val id: Int
    abstract val amount: Double
    abstract val createdAt: Long
    abstract val name: String

    data class ExpenseTransaction(val expense: Expense) : Transaction() {
        override val id = expense.id
        override val amount = expense.amount
        override val createdAt = expense.createdAt
        override val name = expense.name
        val category = expense.category
        val recurrence = expense.recurrence
    }

    data class IncomeTransaction(val income: Income) : Transaction() {
        override val id = income.id
        override val amount = income.amount
        override val createdAt = income.createdAt
        override val name = income.name
        val recurrence = income.recurrence
    }

    data class BudgetTransaction(val budget: Budget) : Transaction() {
        override val id = budget.id
        override val amount = budget.amount
        override val createdAt = 0L
        override val name = budget.name
        val period = budget.period
    }
}
