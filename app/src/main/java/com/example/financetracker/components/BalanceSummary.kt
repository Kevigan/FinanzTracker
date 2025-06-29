package com.example.financetracker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financetracker.data.Transaction

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