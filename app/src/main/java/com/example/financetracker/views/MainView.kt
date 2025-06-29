package com.example.financetracker.views

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financetracker.Screen
import com.example.financetracker.components.BalanceSummary
import com.example.financetracker.components.BudgetSummary
import com.example.financetracker.components.ExpenseLineChart
import com.example.financetracker.components.TransactionBottomBar
import com.example.financetracker.components.TransactionHeaderSection
import com.example.financetracker.components.TransactionListSection
import com.example.financetracker.data.ExpenseCategory
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.data.SortOption
import com.example.financetracker.data.Transaction
import com.example.financetracker.utils.formatDate
import com.example.financetracker.viewModels.FinanzeViewModel
import java.util.Calendar
import java.util.Date


@Composable
fun MainView(
    viewModel: FinanzeViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    var showDateDialog by remember { mutableStateOf(false) }
    var showCalenderDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showRecurrenceFilterDialog by remember { mutableStateOf(false) }
    var showCategoryFilterDialog by remember { mutableStateOf(false) }
    var showTypeFilterDialog by remember { mutableStateOf(false) }

    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var fromDate by remember { mutableStateOf<Date?>(null) }
    var toDate by remember { mutableStateOf<Date?>(null) }
    var selectedRecurrenceFilter by remember { mutableStateOf<RecurrenceType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedTransactionTypeFilter by remember { mutableStateOf<String?>(null) }

    val budgetStatus by viewModel.budgetStatus.collectAsState()

    val transactions by viewModel.simulatedTransactions.collectAsState()

    val filteredAndSortedTransactions by remember(
        transactions, selectedSortOption, fromDate, toDate,
        selectedRecurrenceFilter, selectedCategoryFilter, selectedTransactionTypeFilter
    ) {
        derivedStateOf {
            val from = fromDate?.time ?: Long.MIN_VALUE
            val to = toDate?.time ?: Long.MAX_VALUE
            val typeFilter = selectedTransactionTypeFilter?.lowercase()

            val filtered = transactions.filter { tx ->
                val matchesDate = tx.createdAt in from..to

                val matchesRecurrence = when (tx) {
                    is Transaction.ExpenseTransaction -> selectedRecurrenceFilter == null || tx.expense.recurrence == selectedRecurrenceFilter
                    is Transaction.IncomeTransaction -> selectedRecurrenceFilter == null || tx.income.recurrence == selectedRecurrenceFilter
                    else -> true
                }

                val matchesCategory = when (tx) {
                    is Transaction.ExpenseTransaction -> selectedCategoryFilter == null || tx.expense.category == selectedCategoryFilter
                    else -> selectedCategoryFilter == null
                }

                val matchesType = when (typeFilter) {
                    "income" -> tx is Transaction.IncomeTransaction
                    "expense" -> tx is Transaction.ExpenseTransaction
                    else -> true
                }

                matchesDate && matchesRecurrence && matchesCategory && matchesType
            }

            when (selectedSortOption) {
                SortOption.DATE_ASC -> filtered.sortedBy { it.createdAt }
                SortOption.DATE_DESC -> filtered.sortedByDescending { it.createdAt }
                SortOption.AMOUNT_ASC -> filtered.sortedBy { it.amount }
                SortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
                SortOption.NAME_ASC -> filtered.sortedBy { it.name }
                SortOption.NAME_DESC -> filtered.sortedByDescending { it.name }
            }
        }
    }

    var showSortDialog by remember { mutableStateOf(false) }
    var showChart by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Tracker") },
                actions = {
                    TextButton(onClick = { viewModel.simulateNextDay() }) {
                        Text("Simulate", color = Color.White)
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {
            TransactionBottomBar(
                onSortClick = { showSortDialog = true },
                onFilterClick = { showFilterDialog = true },
                onChartToggle = { showChart = !showChart },
                onEditClick = { navController.navigate(Screen.EditTransactionScreen.route) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(bottom = 8.dp)
        ) {
            BalanceSummary(transactions = transactions)
            BudgetSummary(budgetStatus)
            TransactionHeaderSection(
                selectedSortOption = selectedSortOption,
                fromDate = fromDate,
                toDate = toDate,
                selectedRecurrenceFilter = selectedRecurrenceFilter,
                selectedCategoryFilter = selectedCategoryFilter,
                selectedTransactionTypeFilter = selectedTransactionTypeFilter,
                onResetSort = { selectedSortOption = SortOption.DATE_DESC },
                onResetDateFilter = {
                    fromDate = null
                    toDate = null
                },
                onResetAdvancedFilters = {
                    selectedRecurrenceFilter = null
                    selectedCategoryFilter = null
                    selectedTransactionTypeFilter = null
                }
            )

            TransactionListSection(filteredAndSortedTransactions)
        }
    }

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort Expenses") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    SortOption.entries.forEach { option ->
                        TextButton(onClick = {
                            selectedSortOption = option
                            showSortDialog = false
                        }) {
                            Text(option.label)
                        }
                    }
                }
            }
        )
    }

    if (showCalenderDialog) {
        AlertDialog(
            onDismissRequest = { showCalenderDialog = false },
            title = { Text("Select Date Range") },
            text = {
                Column {
                    Text("From: ${fromDate?.let { formatDate(it.time) } ?: "Not selected"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("To: ${toDate?.let { formatDate(it.time) } ?: "Not selected"}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCalenderDialog = false
                    // You could trigger filtering logic here
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalenderDialog = false }) {
                    Text("Cancel")
                }
            }
        )

        // Show date pickers (optional: move into dialog or buttons)
        LaunchedEffect(Unit) {
            val fromCal = Calendar.getInstance()
            val toCal = Calendar.getInstance()

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    fromCal.set(year, month, dayOfMonth)
                    fromDate = fromCal.time

                    // After fromDate is picked, open toDate picker
                    DatePickerDialog(
                        context,
                        { _, toYear, toMonth, toDay ->
                            toCal.set(toYear, toMonth, toDay)
                            toDate = toCal.time
                        },
                        toCal.get(Calendar.YEAR),
                        toCal.get(Calendar.MONTH),
                        toCal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                fromCal.get(Calendar.YEAR),
                fromCal.get(Calendar.MONTH),
                fromCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    if (showChart) {
        BackHandler {
            showChart = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showChart = false }
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            ) {
                val filteredExpenses =
                    filteredAndSortedTransactions.filterIsInstance<Transaction.ExpenseTransaction>()
                        .map { it.expense }
                ExpenseLineChart(filteredExpenses)

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showChart = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Transaction") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(onClick = {
                        showAddDialog = false
                        navController.navigate(Screen.AddExpenseScreen.route)
                    }) {
                        Text("Add Expense")
                    }
                    TextButton(onClick = {
                        showAddDialog = false
                        navController.navigate(Screen.AddIncomeScreen.route)
                    }) {
                        Text("Add Income")
                    }
                    TextButton(onClick = {
                        showAddDialog = false
                        navController.navigate(Screen.SetBudgetScreen.route)
                    }) {
                        Text("Set Budget")
                    }
                }
            }
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Select Filter Type") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(onClick = {
                        showFilterDialog = false
                        showCalenderDialog = true
                    }) {
                        Text("Filter by Date")
                    }
                    TextButton(onClick = {
                        showFilterDialog = false
                        showRecurrenceFilterDialog = true
                    }) {
                        Text("Filter by Recurrence")
                    }
                    TextButton(onClick = {
                        showFilterDialog = false
                        showCategoryFilterDialog = true
                    }) {
                        Text("Filter by Category")
                    }
                    TextButton(onClick = {
                        showFilterDialog = false
                        showTypeFilterDialog = true
                    }) {
                        Text("Filter by Type")
                    }
                }
            }
        )
    }

    if (showRecurrenceFilterDialog) {
        AlertDialog(
            onDismissRequest = { showRecurrenceFilterDialog = false },
            title = { Text("Select Recurrence") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecurrenceType.entries.forEach { type ->
                        TextButton(onClick = {
                            selectedRecurrenceFilter = type
                            showRecurrenceFilterDialog = false
                        }) {
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                    TextButton(onClick = {
                        selectedRecurrenceFilter = null
                        showRecurrenceFilterDialog = false
                    }) {
                        Text("Clear Filter")
                    }
                }
            }
        )
    }

    if (showCategoryFilterDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryFilterDialog = false },
            title = { Text("Select Category") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    ExpenseCategory.entries.forEach { cat ->
                        TextButton(onClick = {
                            selectedCategoryFilter = cat
                            showCategoryFilterDialog = false
                        }) {
                            Text(cat.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                    TextButton(onClick = {
                        selectedCategoryFilter = null
                        showCategoryFilterDialog = false
                    }) {
                        Text("Clear Filter")
                    }
                }
            }
        )
    }

    if (showTypeFilterDialog) {
        AlertDialog(
            onDismissRequest = { showTypeFilterDialog = false },
            title = { Text("Select Transaction Type") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    listOf("Income", "Expense").forEach { type ->
                        TextButton(onClick = {
                            selectedTransactionTypeFilter = type
                            showTypeFilterDialog = false
                        }) {
                            Text(type)
                        }
                    }
                    TextButton(onClick = {
                        selectedTransactionTypeFilter = null
                        showTypeFilterDialog = false
                    }) {
                        Text("Clear Filter")
                    }
                }
            }
        )
    }
}