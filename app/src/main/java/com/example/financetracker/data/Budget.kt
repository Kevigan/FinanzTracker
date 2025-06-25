package com.example.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: Int = 0,
    val name: String = "",
    val amount: Double,
    val period: BudgetPeriod
)

