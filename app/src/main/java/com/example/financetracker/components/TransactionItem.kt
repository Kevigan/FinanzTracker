package com.example.financetracker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.financetracker.data.ExpenseCategory
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.utils.formatDate

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