package com.example.financetracker.data

enum class SortOption(val label: String) {
    DATE_ASC("Date: Oldest First"),
    DATE_DESC("Date: Newest First"),
    AMOUNT_ASC("Amount: Low to High"),
    AMOUNT_DESC("Amount: High to Low"),
    NAME_ASC("Name: A → Z"),
    NAME_DESC("Name: Z → A")
}
