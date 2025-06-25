package com.example.financetracker.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.example.financetracker.data.RecurrenceMonthDay
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.viewModels.FinanzeViewModel

@Composable
fun AddIncomeView(
    viewModel: FinanzeViewModel,
    navController: NavController
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var selectedRecurrenceDayOfWeek by remember { mutableStateOf<Int?>(null) }
    var selectedRecurrenceMonthDay by remember { mutableStateOf<RecurrenceMonthDay?>(null) }

    var showRecurrenceDialog by remember { mutableStateOf(false) }
    var showDayOfWeekDialog by remember { mutableStateOf(false) }
    var showMonthDayDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Income Name") },
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
                .clickable { showRecurrenceDialog = true }
                .padding(vertical = 12.dp)
        ) {
            Text("Recurrence: ${selectedRecurrence.name}", style = MaterialTheme.typography.body1)
        }

        if (selectedRecurrence == RecurrenceType.WEEKLY) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDayOfWeekDialog = true }
                    .padding(vertical = 12.dp)
            ) {
                val day = selectedRecurrenceDayOfWeek?.let { dayOfWeekToName(it) } ?: "Select day"
                Text("Weekly on: $day", style = MaterialTheme.typography.body1)
            }
        }

        if (selectedRecurrence == RecurrenceType.MONTHLY) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMonthDayDialog = true }
                    .padding(vertical = 12.dp)
            ) {
                val day = selectedRecurrenceMonthDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Select"
                Text("Monthly on: $day", style = MaterialTheme.typography.body1)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountDouble = amount.toDoubleOrNull()
                if (name.isNotBlank() && amountDouble != null) {
                    viewModel.addIncome(
                        name = name,
                        amount = amountDouble,
                        recurrence = selectedRecurrence,
                        recurrenceDayOfWeek = selectedRecurrenceDayOfWeek,
                        recurrenceDayOfMonth = selectedRecurrenceMonthDay
                    )
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Income")
        }
    }

    // Recurrence selection
    if (showRecurrenceDialog) {
        AlertDialog(
            onDismissRequest = { showRecurrenceDialog = false },
            title = { Text("Select Recurrence") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecurrenceType.entries.forEach { type ->
                        TextButton(onClick = {
                            selectedRecurrence = type
                            if (type != RecurrenceType.WEEKLY) selectedRecurrenceDayOfWeek = null
                            if (type != RecurrenceType.MONTHLY) selectedRecurrenceMonthDay = null
                            showRecurrenceDialog = false
                        }) {
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }

    // Weekly day selection
    if (showDayOfWeekDialog) {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        AlertDialog(
            onDismissRequest = { showDayOfWeekDialog = false },
            title = { Text("Select Day of Week") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    days.forEachIndexed { index, label ->
                        TextButton(onClick = {
                            selectedRecurrenceDayOfWeek = index + 2 // Calendar.MONDAY = 2
                            showDayOfWeekDialog = false
                        }) {
                            Text(label)
                        }
                    }
                }
            }
        )
    }

    // Monthly recurrence day selection
    if (showMonthDayDialog) {
        AlertDialog(
            onDismissRequest = { showMonthDayDialog = false },
            title = { Text("Select Monthly Day") },
            buttons = {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecurrenceMonthDay.entries.forEach { day ->
                        TextButton(onClick = {
                            selectedRecurrenceMonthDay = day
                            showMonthDayDialog = false
                        }) {
                            Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }
}

fun dayOfWeekToName(day: Int): String = when (day) {
    java.util.Calendar.MONDAY -> "Monday"
    java.util.Calendar.TUESDAY -> "Tuesday"
    java.util.Calendar.WEDNESDAY -> "Wednesday"
    java.util.Calendar.THURSDAY -> "Thursday"
    java.util.Calendar.FRIDAY -> "Friday"
    java.util.Calendar.SATURDAY -> "Saturday"
    java.util.Calendar.SUNDAY -> "Sunday"
    else -> "Unknown"
}

