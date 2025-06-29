package com.example.financetracker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financetracker.data.ExpenseCategory
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.data.SortOption
import com.example.financetracker.utils.formatDateOnly
import java.util.Date

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
