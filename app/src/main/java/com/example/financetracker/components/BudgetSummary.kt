package com.example.financetracker.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financetracker.viewModels.BudgetStatus

@Composable
fun BudgetSummary(status: BudgetStatus) {
    when (status) {
        is BudgetStatus.NoBudget -> {
            return
        }

        is BudgetStatus.HasBudget -> {
            if (status.budgetAmount == 0.0) return

            val bgColor = if (status.isOverBudget) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
            val text = "Spent: ${"%.2f".format(status.spentThisMonth)}€ / ${"%.2f".format(status.budgetAmount)}€"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = bgColor,
                elevation = 4.dp
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

