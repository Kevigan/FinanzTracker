package com.example.financetracker.views

import android.app.DatePickerDialog
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.financetracker.Screen
import com.example.financetracker.data.Expense
import com.example.financetracker.data.SortOption
import com.example.financetracker.utils.formatDate
import com.example.financetracker.utils.formatDateOnly
import com.example.financetracker.viewModels.ExpenseViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date


@Composable
fun MainView(
    viewModel: ExpenseViewModel,
    navController: NavController
) {
    // Commented out: actual ViewModel data
    // val expenses by viewModel.expenses.collectAsState()
    val context = LocalContext.current
    var fromDate by remember { mutableStateOf<Date?>(null) }
    var toDate by remember { mutableStateOf<Date?>(null) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showCalenderDialog by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_DESC) }

    // 🧪 Dummy data using a for-loop
    val rawExpenses = remember { generateFakeExpenses(100) }

    val filteredAndSortedExpenses by remember(
        rawExpenses, selectedSortOption, fromDate, toDate
    ) {
        derivedStateOf {
            val filtered = rawExpenses.filter { expense ->
                val date = expense.createdAt
                val from = fromDate?.time ?: Long.MIN_VALUE
                val to = toDate?.time ?: Long.MAX_VALUE
                date in from..to
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
                title = { Text("Finance Tracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddExpenseScreen.route)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showSortDialog = true }) {
                        Text("Sort By", style = MaterialTheme.typography.body1, color = Color.White)
                    }
                    TextButton(onClick = { showCalenderDialog = true }) {
                        Text(
                            "Filter Date",
                            style = MaterialTheme.typography.body1,
                            color = Color.White
                        )
                    }
                    TextButton(onClick = { showChart = !showChart }) {
                        Text(
                            "Show Chart",
                            style = MaterialTheme.typography.body1,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F3))
                    .padding(horizontal = 12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Row 1 - Sort Info
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
                            onClick = { selectedSortOption = SortOption.DATE_DESC },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Reset", style = MaterialTheme.typography.caption)
                        }
                    }

                    // Row 2 - Filter Info
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
                                    append("Filtered by date: ")
                                    append(fromDate?.let { formatDateOnly(it.time) } ?: "Start")
                                    append(" to ")
                                    append(toDate?.let { formatDateOnly(it.time) } ?: "End")
                                },
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                            TextButton(
                                onClick = {
                                    fromDate = null
                                    toDate = null
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Reset", style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                }
            }

            // 🧾 Expenses List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredAndSortedExpenses) { expense ->
                    ExpenseItem(expense)
                }
            }
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
                ExpenseLineChart(filteredAndSortedExpenses)

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
}

@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        elevation = 4.dp,
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
                    text = expense.name,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "$${expense.amount}",
                    style = MaterialTheme.typography.subtitle1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDate(expense.createdAt),
                style = MaterialTheme.typography.caption
            )
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

fun generateFakeExpenses(
    count: Int,
    startTime: Long = System.currentTimeMillis()
): List<Expense> {
    val oneDay = 24 * 60 * 60 * 1000L
    return List(count) { index ->
        Expense(
            name = "Fake Expense #$index",
            amount = (5..500).random().toDouble(),
            createdAt = startTime - oneDay * index
        )
    }
}

