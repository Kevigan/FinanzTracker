package com.example.financetracker

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("main_screen", "Main")
    object AddExpenseScreen : Screen("add_expense_screen", "AddExpense")

}