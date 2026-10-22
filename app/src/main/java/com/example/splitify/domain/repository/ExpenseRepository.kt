package com.example.splitify.domain.repository

import com.example.splitify.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    fun getExpensesForFriend(friendId: Long): Flow<List<Expense>>
    fun getTotalOwedForFriend(friendId: Long): Flow<Double?>
    suspend fun saveExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
}
