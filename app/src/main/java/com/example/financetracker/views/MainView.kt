package com.example.financetracker.views

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.financetracker.Screen
import com.example.financetracker.data.Expense
import com.example.financetracker.data.ExpenseCategory
import com.example.financetracker.data.FinanceRepository
import com.example.financetracker.data.Income
import com.example.financetracker.data.IncomeDao
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.data.SortOption
import com.example.financetracker.data.Transaction
import com.example.financetracker.utils.formatDate
import com.example.financetracker.utils.formatDateOnly
import com.example.financetracker.viewModels.FinanzeViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date


@Composable
fun MainView(
    viewModel: FinanzeViewModel,
    navController: NavController
) {
    // Commented out: actual ViewModel data
    // val expenses by viewModel.expenses.collectAsState()
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

    val expenses by viewModel.expenses.collectAsState()
    val incomes by viewModel.incomes.collectAsState()

   // val transactions by viewModel.mockTransactions.collectAsState()
   // val transactions by viewModel.mockTransactions2.collectAsState()
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
                onFilterClick = { showFilterDialog  = true },
                onChartToggle = { showChart = !showChart }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(bottom = 8.dp)
        ) {
            BalanceSummary(transactions = filteredAndSortedTransactions)

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
                val filteredExpenses = filteredAndSortedTransactions.filterIsInstance<Transaction.ExpenseTransaction>().map { it.expense }
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

@Composable
fun TransactionItem(
    name: String,
    amount: Double,
    createdAt: Long,
    isIncome: Boolean,
    category: ExpenseCategory? = null,
    recurrence: RecurrenceType = RecurrenceType.NONE
) {
    val amountText = if (isIncome) "+$${amount}" else "$${amount}"
    val amountColor = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colors.onSurface
    val backgroundColor = if (isIncome) Color(0xFFE8F5E9) else MaterialTheme.colors.surface

    Card(
        elevation = 4.dp,
        backgroundColor = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.subtitle1,
                    color = amountColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDate(createdAt),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier.weight(2f)
                )

                Text(
                    text = category?.name
                        ?.lowercase()
                        ?.replaceFirstChar { it.uppercase() } ?: "",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Text(
                    text = if (recurrence != RecurrenceType.NONE)
                        recurrence.name.lowercase().replaceFirstChar { it.uppercase() }
                    else "",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ExpenseLineChart(expenses: List<Expense>) {
    if (expenses.isEmpty()) return

    val entries = expenses.mapIndexed { index, expense ->
        Entry(index.toFloat(), expense.amount.toFloat())
    }

    val average = expenses.map { it.amount }.average().toFloat()

    val avgEntries = expenses.indices.map { index ->
        Entry(index.toFloat(), average)
    }

    val labels = expenses.map { expense ->
        val formatter = java.text.SimpleDateFormat("MMM yy", java.util.Locale.getDefault())
        formatter.format(Date(expense.createdAt))
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Main line dataset
                val dataSet = LineDataSet(entries, "Expenses").apply {
                    color = android.graphics.Color.BLUE
                    valueTextColor = android.graphics.Color.BLACK
                    setDrawCircles(true)
                    lineWidth = 2f
                    setDrawFilled(true)
                    fillColor = android.graphics.Color.LTGRAY
                    setDrawValues(false)
                }

                // Average line dataset
                val avgDataSet = LineDataSet(avgEntries, "Average").apply {
                    color = android.graphics.Color.RED
                    setDrawCircles(false)
                    lineWidth = 2f
                    enableDashedLine(10f, 10f, 0f)
                    setDrawValues(false)
                }

                this.data = LineData(dataSet, avgDataSet)

                // Y-axis formatting
                axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}€"
                    }
                }

                // X-axis formatting
                xAxis.apply {
                    granularity = 1f
                    labelRotationAngle = -45f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt().coerceIn(labels.indices)
                            return labels[index]
                        }
                    }
                    setDrawGridLines(false)
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                }

                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = true
                setBackgroundColor(android.graphics.Color.WHITE)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(250.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun TransactionHeaderSection(
    selectedSortOption: SortOption,
    fromDate: Date?,
    toDate: Date?,
    selectedRecurrenceFilter: RecurrenceType?,
    selectedCategoryFilter: ExpenseCategory?,
    selectedTransactionTypeFilter: String?,
    onResetSort: () -> Unit,
    onResetDateFilter: () -> Unit,
    onResetAdvancedFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F3))
            .padding(horizontal = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            // Sort section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = "Sorted by: ${selectedSortOption.label}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                TextButton(
                    onClick = onResetSort,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Reset", style = MaterialTheme.typography.caption)
                }
            }

            // Date filter section
            if (fromDate != null || toDate != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = buildString {
                            append("Date: ")
                            append(fromDate?.let { formatDateOnly(it.time) } ?: "Start")
                            append(" to ")
                            append(toDate?.let { formatDateOnly(it.time) } ?: "End")
                        },
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    TextButton(
                        onClick = onResetDateFilter,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Reset", style = MaterialTheme.typography.caption)
                    }
                }
            }

            // Recurrence / Category / Type filter section
            if (selectedRecurrenceFilter != null || selectedCategoryFilter != null || selectedTransactionTypeFilter != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    val activeFilters = buildList {
                        if (selectedRecurrenceFilter != null) add(selectedRecurrenceFilter.name.lowercase().replaceFirstChar { it.uppercase() })
                        if (selectedCategoryFilter != null) add(selectedCategoryFilter.name.lowercase().replaceFirstChar { it.uppercase() })
                        if (selectedTransactionTypeFilter != null) add(selectedTransactionTypeFilter.lowercase().replaceFirstChar { it.uppercase() })
                    }.joinToString(", ")

                    Text(
                        text = "Filter: $activeFilters",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    TextButton(
                        onClick = onResetAdvancedFilters,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Reset", style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListSection(transactions: List<Transaction>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(transactions) { transaction ->
            when (transaction) {
                is Transaction.ExpenseTransaction -> TransactionItem(
                    name = transaction.name,
                    amount = transaction.amount,
                    createdAt = transaction.createdAt,
                    isIncome = false,
                    category = transaction.expense.category,
                    recurrence = transaction.expense.recurrence
                )
                is Transaction.IncomeTransaction -> TransactionItem(
                    name = transaction.name,
                    amount = transaction.amount,
                    createdAt = transaction.createdAt,
                    isIncome = true,
                    category = null,
                    recurrence = transaction.income.recurrence
                )
                is Transaction.BudgetTransaction -> {
                    // Not yet implemented, but satisfies exhaustiveness requirement
                }
            }
        }
    }
}

@Composable
fun TransactionBottomBar(
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onChartToggle: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onSortClick) {
                Text("Sort By", style = MaterialTheme.typography.body1, color = Color.White)
            }
            TextButton(onClick = onFilterClick) {
                Text("Filter", style = MaterialTheme.typography.body1, color = Color.White)
            }
            TextButton(onClick = onChartToggle) {
                Text("Show Chart", style = MaterialTheme.typography.body1, color = Color.White)
            }
        }
    }
}

@Composable
fun BalanceSummary(transactions: List<Transaction>) {
    val totalIncome = transactions
        .filterIsInstance<Transaction.IncomeTransaction>()
        .sumOf { it.income.amount }

    val totalExpense = transactions
        .filterIsInstance<Transaction.ExpenseTransaction>()
        .sumOf { it.expense.amount }

    val balance = totalIncome - totalExpense
    val isPositive = balance >= 0

    Card(
        backgroundColor = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Current Balance", style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "€%.2f".format(balance),
                style = MaterialTheme.typography.h6,
                color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}





