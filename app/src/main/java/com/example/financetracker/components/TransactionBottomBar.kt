package com.example.financetracker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TransactionBottomBar(
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onChartToggle: () -> Unit,
    onEditClick: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onSortClick) {
                Text("Sort By", style = MaterialTheme.typography.body1, color = Color.White)
            }
            TextButton(onClick = onFilterClick) {
                Text("Filter", style = MaterialTheme.typography.body1, color = Color.White)
            }
            TextButton(onClick = onChartToggle) {
                Text("Show Chart", style = MaterialTheme.typography.body1, color = Color.White)
            }
            TextButton(onClick = onEditClick) {
                Text("Edit", style = MaterialTheme.typography.body1, color = Color.White)
            }
        }
    }
}