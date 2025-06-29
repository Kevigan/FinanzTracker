package com.example.financetracker.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.financetracker.data.Expense
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Date

@Composable
fun ExpenseLineChart(expenses: List<Expense>) {
    if (expenses.isEmpty()) return

    val sortedExpenses = expenses.sortedBy { it.createdAt } // oldest to newest

    val entries = sortedExpenses.mapIndexed { index, expense ->
        Entry(index.toFloat(), expense.amount.toFloat())
    }

    val average = sortedExpenses.map { it.amount }.average().toFloat()

    val avgEntries = sortedExpenses.indices.map { index ->
        Entry(index.toFloat(), average)
    }

    val labels = sortedExpenses.map { expense ->
        val formatter = java.text.SimpleDateFormat("MMM yy", java.util.Locale.getDefault())
        formatter.format(Date(expense.createdAt))
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Main line dataset
                val dataSet = LineDataSet(entries, "Expenses").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                    setDrawCircles(true)
                    lineWidth = 2f
                    setDrawFilled(true)
                    fillColor = Color.LTGRAY
                    setDrawValues(false)
                }

                // Average line dataset
                val avgDataSet = LineDataSet(avgEntries, "Average").apply {
                    color = Color.RED
                    setDrawCircles(false)
                    lineWidth = 2f
                    enableDashedLine(10f, 10f, 0f)
                    setDrawValues(false)
                }

                this.data = LineData(dataSet, avgDataSet)

                // Y-axis formatting
                axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}€"
                    }
                }

                // X-axis formatting
                xAxis.apply {
                    granularity = 1f
                    labelRotationAngle = -45f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt().coerceIn(labels.indices)
                            return labels[index]
                        }
                    }
                    setDrawGridLines(false)
                    position = XAxis.XAxisPosition.BOTTOM
                }

                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = true
                setBackgroundColor(Color.WHITE)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(250.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}