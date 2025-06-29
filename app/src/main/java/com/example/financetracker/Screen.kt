package com.example.financetracker

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("main_screen", "Main")
    object AddExpenseScreen : Screen("add_expense_screen", "AddExpense")
    object AddIncomeScreen : Screen("add_income_screen", "AddIncome")
    object EditTransactionScreen  : Screen("edit_transactions_screen", "EditTransactions")
    object SetBudgetScreen  : Screen("set_budget_screen", "SetBudget")

}