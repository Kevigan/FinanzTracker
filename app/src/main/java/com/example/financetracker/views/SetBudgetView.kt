package com.example.financetracker.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.financetracker.data.BudgetPeriod
import com.example.financetracker.viewModels.FinanzeViewModel

@Composable
fun SetBudgetView(
    viewModel: FinanzeViewModel,
    navController: NavController
) {
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Set Budget", style = MaterialTheme.typography.h6)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Budget Amount (€)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Period:")
        BudgetPeriod.entries.forEach { period ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)) {
                RadioButton(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period }
                )
                Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val value = amount.toDoubleOrNull()
                if (value != null) {
                    viewModel.setBudget(value, selectedPeriod)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Budget")
        }
    }
}
