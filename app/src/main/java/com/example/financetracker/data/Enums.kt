package com.example.financetracker.data

enum class RecurrenceType { NONE, WEEKLY, MONTHLY }

enum class RecurrenceMonthDay { FIRST, MID, LAST }

enum class BudgetPeriod { WEEKLY, MONTHLY }

enum class ExpenseCategory {
    FOOD,
    TRANSPORT,
    BILLS,
    ENTERTAINMENT,
    OTHER;

    val label: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}