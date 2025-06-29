package com.example.financetracker.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financetracker.data.BudgetPeriod
import com.example.financetracker.data.Expense
import com.example.financetracker.data.Income
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.viewModels.FinanzeViewModel

@Composable
fun EditTransactionView(viewModel: FinanzeViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val budget by viewModel.budget.collectAsState()

    var selectedIncome by remember { mutableStateOf<Income?>(null) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recurring Transactions") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Incomes", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TransactionListBox(
                    title = "Weekly Incomes",
                    items = incomes.filter { it.recurrence == RecurrenceType.WEEKLY },
                    onItemClick = { selectedIncome = it }
                )
                TransactionListBox(
                    title = "Monthly Incomes",
                    items = incomes.filter { it.recurrence == RecurrenceType.MONTHLY },
                    onItemClick = { selectedIncome = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Expenses", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TransactionListBox(
                    title = "Weekly Expenses",
                    items = expenses.filter { it.recurrence == RecurrenceType.WEEKLY },
                    onItemClick = { selectedExpense = it }
                )
                TransactionListBox(
                    title = "Monthly Expenses",
                    items = expenses.filter { it.recurrence == RecurrenceType.MONTHLY },
                    onItemClick = { selectedExpense = it }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text("Budget", style = MaterialTheme.typography.h6)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(6.dp, shape = MaterialTheme.shapes.medium)
                    .background(Color(0xFFEDE7F6), shape = MaterialTheme.shapes.medium)
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
                    .clickable { showBudgetDialog = true }
                    .padding(16.dp)
            ) {
                Column {
                    Text("Budget", style = MaterialTheme.typography.subtitle1)

                    if (budget != null) {
                        Text("Amount: €${"%.2f".format(budget!!.amount)}", style = MaterialTheme.typography.body1)
                        Text("Period: ${budget!!.period.name}", style = MaterialTheme.typography.body2)
                    } else {
                        Text("No budget set", style = MaterialTheme.typography.body2, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Dialogs
    selectedIncome?.let {
        EditTransactionDialog(
            name = it.name,
            amount = it.amount,
            recurrence = it.recurrence,
            onConfirm = { newAmount, newRecurrence ->
                viewModel.updateIncome(it.copy(amount = newAmount, recurrence = newRecurrence))
                selectedIncome = null
            },
            onDelete = {
                viewModel.deleteIncome(it)
                selectedIncome = null
            },
            onDismiss = { selectedIncome = null }
        )
    }

    selectedExpense?.let {
        EditTransactionDialog(
            name = it.name,
            amount = it.amount,
            recurrence = it.recurrence,
            onConfirm = { newAmount, newRecurrence ->
                viewModel.updateExpense(it.copy(amount = newAmount, recurrence = newRecurrence))
                selectedExpense = null
            },
            onDelete = {
                viewModel.deleteExpense(it)
                selectedExpense = null
            },
            onDismiss = { selectedExpense = null }
        )
    }

    if (showBudgetDialog && budget != null) {
        var newAmount by remember { mutableStateOf(budget!!.amount.toString()) }
        var selectedPeriod by remember { mutableStateOf(budget!!.period) }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Edit Budget") },
            text = {
                Column {
                    TextField(
                        value = newAmount,
                        onValueChange = { newAmount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Period:")
                    BudgetPeriod.entries.forEach { period ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPeriod = period }
                                .padding(4.dp)
                        ) {
                            RadioButton(
                                selected = period == selectedPeriod,
                                onClick = { selectedPeriod = period }
                            )
                            Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = newAmount.toDoubleOrNull()
                    if (amt != null) {
                        viewModel.updateBudget(budget!!.copy(amount = amt, period = selectedPeriod))
                        showBudgetDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
fun <T> TransactionListBox(
    title: String,
    items: List<T>,
    onItemClick: (T) -> Unit
) where T : Any {
    Box(
        modifier = Modifier
            .width(170.dp)
            .height(250.dp)
            .padding(4.dp)
            .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium)
            .background(color = Color(0xFFF8F8F8), shape = MaterialTheme.shapes.medium)
            .border(width = 1.dp, color = Color.LightGray, shape = MaterialTheme.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onItemClick(item) },
                        elevation = 2.dp
                    ) {
                        val name = when (item) {
                            is Income -> item.name
                            is Expense -> item.name
                            else -> ""
                        }
                        val amount = when (item) {
                            is Income -> item.amount
                            is Expense -> item.amount
                            else -> 0.0
                        }

                        Text(
                            text = "$name: €${"%.2f".format(amount)}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    name: String,
    amount: Double,
    recurrence: RecurrenceType,
    onConfirm: (Double, RecurrenceType) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var editedAmount by remember { mutableStateOf(amount.toString()) }
    var selectedRecurrence by remember { mutableStateOf(recurrence) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $name") },
        text = {
            Column {
                TextField(
                    value = editedAmount,
                    onValueChange = { editedAmount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Recurrence:")
                RecurrenceType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRecurrence = type }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRecurrence == type,
                            onClick = { selectedRecurrence = type }
                        )
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amountDouble = editedAmount.toDoubleOrNull()
                if (amountDouble != null) {
                    onConfirm(amountDouble, selectedRecurrence)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
