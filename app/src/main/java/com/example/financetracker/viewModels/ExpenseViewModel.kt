package com.example.financetracker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Expense
import com.example.financetracker.data.ExpenseRepository
import com.example.financetracker.data.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption = _sortOption.asStateFlow()

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    val expenses = combine(
        repository.getExpenses(),
        sortOption
    ) { expenses, sort ->
        when (sort) {
            SortOption.DATE_ASC -> expenses.sortedBy { it.createdAt }
            SortOption.DATE_DESC -> expenses.sortedByDescending { it.createdAt }
            SortOption.AMOUNT_ASC -> expenses.sortedBy { it.amount }
            SortOption.AMOUNT_DESC -> expenses.sortedByDescending { it.amount }
            SortOption.NAME_ASC -> expenses.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> expenses.sortedByDescending { it.name.lowercase() }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    fun addExpense(name: String, amount: Double) {
        viewModelScope.launch {
            repository.addExpense(Expense(name = name, amount = amount))
        }
    }
}
