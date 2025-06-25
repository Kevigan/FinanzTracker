package com.example.financetracker

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financetracker.viewModels.FinanzeViewModel
import com.example.financetracker.views.AddExpenseView
import com.example.financetracker.views.AddIncomeView
import com.example.financetracker.views.MainView

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    expenseViewModel: FinanzeViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainView(
                navController = navController,
                viewModel = expenseViewModel
            )
        }

        composable(Screen.AddExpenseScreen.route) {
            AddExpenseView(
                navController = navController,
                viewModel = expenseViewModel
            )
        }

        composable(Screen.AddIncomeScreen.route) {
            AddIncomeView(
                navController = navController,
                viewModel = expenseViewModel
            )
        }
    }
}
