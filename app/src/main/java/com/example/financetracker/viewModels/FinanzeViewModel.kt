package com.example.financetracker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Budget
import com.example.financetracker.data.BudgetPeriod
import com.example.financetracker.data.Expense
import com.example.financetracker.data.ExpenseCategory
import com.example.financetracker.data.FinanceRepository
import com.example.financetracker.data.Income
import com.example.financetracker.data.RecurrenceMonthDay
import com.example.financetracker.data.RecurrenceType
import com.example.financetracker.data.SortOption
import com.example.financetracker.data.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class FinanzeViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption = _sortOption.asStateFlow()

    val incomes = repository.getIncomes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _simulatedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val simulatedTransactions = _simulatedTransactions.asStateFlow()

    private var simulatedDate = System.currentTimeMillis()
    private var budgetStartDate = getFirstOfCurrentMonth(simulatedDate)

    private fun getFirstOfCurrentMonth(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun simulateNextDay() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply { timeInMillis = simulatedDate }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val newTransactions = mutableListOf<Transaction>()

            // Add fake expenses and recurring items
            newTransactions += Transaction.ExpenseTransaction(
                Expense(
                    name = "Daily Expense",
                    amount = (5..10).random().toDouble(),
                    category = ExpenseCategory.FOOD,
                    createdAt = simulatedDate
                )
            )

            if (dayOfWeek == Calendar.MONDAY) {
                newTransactions += Transaction.ExpenseTransaction(
                    Expense(
                        name = "Groceries",
                        amount = (20..40).random().toDouble(),
                        category = ExpenseCategory.FOOD,
                        createdAt = simulatedDate
                    )
                )
            }

            // Add recurring expenses
            expenses.value.filter { it.recurrence != RecurrenceType.NONE }.forEach { exp ->
                val matches = when (exp.recurrence) {
                    RecurrenceType.WEEKLY -> exp.recurrenceDayOfWeek == dayOfWeek
                    RecurrenceType.MONTHLY -> when (exp.recurrenceDayOfMonth) {
                        RecurrenceMonthDay.FIRST -> dayOfMonth == 1
                        RecurrenceMonthDay.MID -> dayOfMonth == 15
                        RecurrenceMonthDay.LAST -> dayOfMonth == maxDay
                        null -> false
                    }
                    else -> false
                }
                if (matches) {
                    newTransactions += Transaction.ExpenseTransaction(exp.copy(createdAt = simulatedDate))
                }
            }

            incomes.value.filter { it.recurrence != RecurrenceType.NONE }.forEach { inc ->
                val matches = when (inc.recurrence) {
                    RecurrenceType.WEEKLY -> inc.recurrenceDayOfWeek == dayOfWeek
                    RecurrenceType.MONTHLY -> when (inc.recurrenceDayOfMonth) {
                        RecurrenceMonthDay.FIRST -> dayOfMonth == 1
                        RecurrenceMonthDay.MID -> dayOfMonth == 15
                        RecurrenceMonthDay.LAST -> dayOfMonth == maxDay
                        null -> false
                    }
                    else -> false
                }
                if (matches) {
                    newTransactions += Transaction.IncomeTransaction(inc.copy(createdAt = simulatedDate))
                }
            }

            // Reset only the budget tracker if it's the 1st
            if (dayOfMonth == 1) {
                budgetStartDate = getFirstOfCurrentMonth(simulatedDate)
            }

            _simulatedTransactions.value = _simulatedTransactions.value + newTransactions
            simulatedDate += 86_400_000L
        }
    }


    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun addExpense(
        name: String,
        amount: Double,
        category: ExpenseCategory,
        recurrence: RecurrenceType = RecurrenceType.NONE,
        recurrenceDayOfWeek: Int? = null,
        recurrenceMonthDay: RecurrenceMonthDay? = null
    ) {
        viewModelScope.launch {
            val expense = Expense(
                name = name,
                amount = amount,
                category = category,
                recurrence = recurrence,
                recurrenceDayOfWeek = recurrenceDayOfWeek,
                recurrenceDayOfMonth = recurrenceMonthDay
            )
            repository.addExpense(expense)
        }
    }

    fun addIncome(
        name: String,
        amount: Double,
        createdAt: Long = System.currentTimeMillis(),
        recurrence: RecurrenceType = RecurrenceType.NONE,
        recurrenceDayOfWeek: Int? = null,
        recurrenceDayOfMonth: RecurrenceMonthDay? = null
    ) {
        viewModelScope.launch {
            val income = Income(
                name = name,
                amount = amount,
                createdAt = createdAt,
                recurrence = recurrence,
                recurrenceDayOfWeek = recurrenceDayOfWeek,
                recurrenceDayOfMonth = recurrenceDayOfMonth
            )
            repository.addIncome(income)
        }
    }

    // Budget section
    val budget = repository.getBudget()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setBudget(
        amount: Double,
        period: BudgetPeriod
    ) {
        viewModelScope.launch {
            repository.setBudget(amount = amount, period = period)
        }
    }

    fun updateBudget(budget: Budget){
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun updateIncome(updated: Income) {
        viewModelScope.launch {
            repository.updateIncome(updated)
        }
    }

    fun updateExpense(updated: Expense) {
        viewModelScope.launch {
            repository.updateExpense(updated)
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
    val budgetStatus = combine(
        budget,
        simulatedTransactions
    ) { budgetOpt, allTxs ->

        if (budgetOpt == null || budgetOpt.amount == 0.0) return@combine BudgetStatus.NoBudget

        val calendar = Calendar.getInstance().apply {
            timeInMillis = simulatedDate // use simulated time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val firstOfMonth = calendar.timeInMillis


        val spentThisMonth = allTxs
            .filterIsInstance<Transaction.ExpenseTransaction>()
            .filter { tx ->
                tx.expense.createdAt >= firstOfMonth &&
                        tx.expense.recurrence == RecurrenceType.NONE
            }
            .sumOf { it.expense.amount }

        BudgetStatus.HasBudget(
            budgetAmount = budgetOpt.amount,
            spentThisMonth = spentThisMonth,
            isOverBudget = spentThisMonth >= budgetOpt.amount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetStatus.NoBudget)
}
sealed class BudgetStatus {
    object NoBudget : BudgetStatus()
    data class HasBudget(
        val budgetAmount: Double,
        val spentThisMonth: Double,
        val isOverBudget: Boolean
    ) : BudgetStatus()
}