package com.example.financetracker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListSection(transactions: List<Transaction>) {
    val sorted = transactions.sortedByDescending { it.createdAt }

    val itemsWithFlags = remember(sorted) {
        sorted.mapIndexed { index, transaction ->
            val currentMonth = getYearMonthLabel(transaction.createdAt)
            val previousMonth = sorted.getOrNull(index - 1)?.createdAt?.let { getYearMonthLabel(it) }
            val showDivider = previousMonth != null && currentMonth != previousMonth
            TransactionWithDivider(
                transaction = transaction,
                showDivider = showDivider,
                currentMonth = previousMonth,
                previousMonth = currentMonth
            )
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(itemsWithFlags) { item ->
            if (item.showDivider) {
                //  Current month label (above divider)
                Text(
                    text = item.currentMonth ?: "",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 2.dp)
                )
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
                //  Previous month label (below divider)
                Text(
                    text = item.previousMonth ?: "",
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, top = 2.dp)
                )
            }

            when (val tx = item.transaction) {
                is Transaction.ExpenseTransaction -> TransactionItem(
                    name = tx.name,
                    amount = tx.amount,
                    createdAt = tx.createdAt,
                    isIncome = false,
                    category = tx.expense.category,
                    recurrence = tx.expense.recurrence
                )
                is Transaction.IncomeTransaction -> TransactionItem(
                    name = tx.name,
                    amount = tx.amount,
                    createdAt = tx.createdAt,
                    isIncome = true,
                    category = null,
                    recurrence = tx.income.recurrence
                )
                is Transaction.BudgetTransaction -> Unit
            }
        }
    }
}

// Convert to "June 2025" format
private fun getYearMonthLabel(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private data class TransactionWithDivider(
    val transaction: Transaction,
    val showDivider: Boolean,
    val currentMonth: String?,
    val previousMonth: String?
)
