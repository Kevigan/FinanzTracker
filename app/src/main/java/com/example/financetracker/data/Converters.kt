package com.example.financetracker.data

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toCategory(name: String): ExpenseCategory = ExpenseCategory.valueOf(name)

    @TypeConverter
    fun fromRecurrence(type: RecurrenceType): String = type.name

    @TypeConverter
    fun toRecurrence(name: String): RecurrenceType = RecurrenceType.valueOf(name)
}