package com.example.financetracker.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financetracker.data.ExpenseCategory
import com.example.financetracker.data.RecurrenceMonthDay
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.viewModels.FinanzeViewModel
import java.util.*

@Composable
fun AddExpenseView(
    viewModel: FinanzeViewModel,
    navController: NavController
) {
    val categoryOptions = ExpenseCategory.entries

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.NONE) }

    var selectedRecurrenceDayOfWeek by remember { mutableStateOf<Int?>(null) }
    var selectedRecurrenceMonthDay by remember { mutableStateOf<RecurrenceMonthDay?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showRecurrenceDialog by remember { mutableStateOf(false) }
    var showDayOfWeekDialog by remember { mutableStateOf(false) }
    var showDayOfMonthDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Expense Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategoryDialog = true }
                .padding(vertical = 12.dp)
        ) {
            Text("Category: $selectedCategory", style = MaterialTheme.typography.body1)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showRecurrenceDialog = true }
                .padding(vertical = 12.dp)
        ) {
            Text("Recurrence: $selectedRecurrence", style = MaterialTheme.typography.body1)
        }

        if (selectedRecurrence == RecurrenceType.WEEKLY) {
            val label = selectedRecurrenceDayOfWeek?.let {
                val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                days[it % 7]
            } ?: "Select Day of Week"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDayOfWeekDialog = true }
                    .padding(vertical = 12.dp)
            ) {
                Text("Repeats on: $label", style = MaterialTheme.typography.body1)
            }
        }

        if (selectedRecurrence == RecurrenceType.MONTHLY) {
            val label = selectedRecurrenceMonthDay?.name
                ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Select Monthly Day"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDayOfMonthDialog = true }
                    .padding(vertical = 12.dp)
            ) {
                Text("Repeats on: $label", style = MaterialTheme.typography.body1)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountDouble = amount.toDoubleOrNull()
                if (name.isNotBlank() && amountDouble != null) {
                    viewModel.addExpense(
                        name = name,
                        amount = amountDouble,
                        category = selectedCategory,
                        recurrence = selectedRecurrence,
                        recurrenceDayOfWeek = selectedRecurrenceDayOfWeek,
                        recurrenceMonthDay = selectedRecurrenceMonthDay
                    )

                    // Reset state
                    name = ""
                    amount = ""
                    selectedCategory = categoryOptions.first()
                    selectedRecurrence = RecurrenceType.NONE
                    selectedRecurrenceDayOfWeek = null
                    selectedRecurrenceMonthDay = null

                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
        }
    }

    // Dialogs
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    categoryOptions.forEach { category ->
                        TextButton(onClick = {
                            selectedCategory = category
                            showCategoryDialog = false
                        }) {
                            Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }

    if (showRecurrenceDialog) {
        AlertDialog(
            onDismissRequest = { showRecurrenceDialog = false },
            title = { Text("Select Recurrence") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecurrenceType.entries.forEach { type ->
                        TextButton(onClick = {
                            selectedRecurrence = type
                            showRecurrenceDialog = false
                        }) {
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }

    if (showDayOfWeekDialog) {
        val days = listOf(
            "Sunday" to Calendar.SUNDAY,
            "Monday" to Calendar.MONDAY,
            "Tuesday" to Calendar.TUESDAY,
            "Wednesday" to Calendar.WEDNESDAY,
            "Thursday" to Calendar.THURSDAY,
            "Friday" to Calendar.FRIDAY,
            "Saturday" to Calendar.SATURDAY,
        )

        AlertDialog(
            onDismissRequest = { showDayOfWeekDialog = false },
            title = { Text("Select Day of Week") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    days.forEach { (label, value) ->
                        TextButton(onClick = {
                            selectedRecurrenceDayOfWeek = value
                            showDayOfWeekDialog = false
                        }) {
                            Text(label)
                        }
                    }
                }
            }
        )
    }

    if (showDayOfMonthDialog) {
        AlertDialog(
            onDismissRequest = { showDayOfMonthDialog = false },
            title = { Text("Select Day of Month") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecurrenceMonthDay.entries.forEach { option ->
                        TextButton(onClick = {
                            selectedRecurrenceMonthDay = option
                            showDayOfMonthDialog = false
                        }) {
                            Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }
}
