package com.example.financetracker.di

import android.content.Context
import androidx.room.Room
import com.example.financetracker.data.BudgetDao
import com.example.financetracker.data.ExpenseDao
import com.example.financetracker.data.ExpenseDatabase
import com.example.financetracker.data.IncomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_db"
        ).fallbackToDestructiveMigration() // Optional: wipe DB on schema mismatch
            .build()
    }

    @Provides
    fun provideExpenseDao(db: ExpenseDatabase): ExpenseDao {
        return db.expenseDao()
    }

    @Provides
    fun provideBudgetDao(db: ExpenseDatabase): BudgetDao {
        return db.budgetDao()
    }

    @Provides
    fun provideIncomeDao(db: ExpenseDatabase): IncomeDao {
        return db.incomeDao()
    }
}

