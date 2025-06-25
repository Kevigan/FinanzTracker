package com.example.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val recurrence: RecurrenceType = RecurrenceType.NONE, // WEEKLY or MONTHLY
    val recurrenceDayOfWeek: Int? = null, // For weekly: Calendar.MONDAY, etc.
    val recurrenceDayOfMonth: RecurrenceMonthDay? = null // For monthly
)

