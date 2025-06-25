package com.example.financetracker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Load real incomes from DB
    val incomes = repository.getIncomes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Load real expenses and allow sorting
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

    // Combined list of fake expenses + real incomes
    val mockTransactions = combine(incomes, expenses) { incomes, expenses ->
        val oneDay = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()

        // Fake expenses
        val fakeExpenses = List(50) { index ->
            Expense(
                name = "Fake Expense #$index",
                amount = (5..500).random().toDouble(),
                category = listOf(
                    ExpenseCategory.FOOD,
                    ExpenseCategory.TRANSPORT,
                    ExpenseCategory.BILLS
                ).random(),
                createdAt = now - oneDay * index,
                recurrence = RecurrenceType.NONE
            )
        }

        // Combine all transactions
        val allTransactions = buildList {
            addAll(fakeExpenses.map { Transaction.ExpenseTransaction(it) })
            addAll(expenses.map { Transaction.ExpenseTransaction(it) })
            addAll(incomes.map { Transaction.IncomeTransaction(it) })
        }

        allTransactions.sortedByDescending { it.createdAt }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val mockTransactions2 = combine(incomes, expenses) { incomes, expenses ->
        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L
        val oneYearAgo = now - (365 * oneDayMillis)

        val random = java.util.Random()

        // --- 1. Daily small expenses ---
        val dailyExpenses = (0 until 365).map { day ->
            val date = oneYearAgo + (day * oneDayMillis)
            Expense(
                name = "Coffee & Snacks",
                amount = 5 + random.nextInt(6).toDouble(), // 5-10€
                category = ExpenseCategory.FOOD,
                createdAt = date,
                recurrence = RecurrenceType.NONE
            )
        }

        // --- 2. Weekly groceries ---
        val weeklyGroceries = (0 until 52).map { week ->
            val date = oneYearAgo + (week * 7 * oneDayMillis)
            Expense(
                name = "Groceries",
                amount = 30 + random.nextInt(21).toDouble(), // 30-50€
                category = ExpenseCategory.FOOD,
                createdAt = date,
                recurrence = RecurrenceType.NONE
            )
        }

        // --- 3. Add recurring expenses from DB ---
        val recurringExpenses = expenses.filter {
            it.recurrence != RecurrenceType.NONE
        }.flatMap { exp ->
            when (exp.recurrence) {
                RecurrenceType.WEEKLY -> (0 until 52).map { week ->
                    exp.copy(createdAt = oneYearAgo + (week * 7 * oneDayMillis))
                }

                RecurrenceType.MONTHLY -> (0 until 12).map { month ->
                    val cal = Calendar.getInstance().apply { timeInMillis = oneYearAgo }
                    cal.add(Calendar.MONTH, month)
                    exp.copy(createdAt = cal.timeInMillis)
                }

                else -> emptyList()
            }
        }

        // --- 4. Add recurring incomes from DB ---
        val recurringIncomes = incomes.filter {
            it.recurrence != RecurrenceType.NONE
        }.flatMap { income ->
            when (income.recurrence) {
                RecurrenceType.WEEKLY -> (0 until 52).map { week ->
                    income.copy(createdAt = oneYearAgo + (week * 7 * oneDayMillis))
                }

                RecurrenceType.MONTHLY -> (0 until 12).map { month ->
                    val cal = Calendar.getInstance().apply { timeInMillis = oneYearAgo }
                    cal.add(Calendar.MONTH, month)
                    income.copy(createdAt = cal.timeInMillis)
                }

                else -> emptyList()
            }
        }

        // --- 5. Combine and wrap into transactions ---
        val allExpenses = dailyExpenses + weeklyGroceries + recurringExpenses
        val expenseTransactions = allExpenses.map { Transaction.ExpenseTransaction(it) }
        val incomeTransactions = recurringIncomes.map { Transaction.IncomeTransaction(it) }

        (expenseTransactions + incomeTransactions)
            .sortedByDescending { it.createdAt }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _simulatedTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val simulatedTransactions = _simulatedTransactions.asStateFlow()

    private var simulatedDate = System.currentTimeMillis()

    fun simulateNextDay() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply { timeInMillis = simulatedDate }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val newTransactions = mutableListOf<Transaction>()

            // Daily small expense
            newTransactions += Transaction.ExpenseTransaction(
                Expense(
                    name = "Daily Expense",
                    amount = (5..10).random().toDouble(),
                    category = ExpenseCategory.FOOD,
                    createdAt = simulatedDate
                )
            )

            // Weekly groceries on Mondays
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

            // Recurring expenses (from DB)
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
                    newTransactions += Transaction.ExpenseTransaction(
                        exp.copy(createdAt = simulatedDate)
                    )
                }
            }

            // Recurring incomes (from DB)
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
                    newTransactions += Transaction.IncomeTransaction(
                        inc.copy(createdAt = simulatedDate)
                    )
                }
            }

            // Update the state
            _simulatedTransactions.value = _simulatedTransactions.value + newTransactions
            simulatedDate += 86_400_000L // Advance one day
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
}
