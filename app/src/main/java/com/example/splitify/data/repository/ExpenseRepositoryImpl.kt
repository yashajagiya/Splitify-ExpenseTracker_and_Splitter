package com.example.splitify.data.repository

import com.example.splitify.data.ExpenseDao
import com.example.splitify.data.mapper.toDomain
import com.example.splitify.data.mapper.toEntity
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }

    override fun getExpensesForFriend(friendId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesForFriend(friendId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTotalOwedForFriend(friendId: Long): Flow<Double?> {
        return expenseDao.getTotalOwedForFriend(friendId)
    }

    override suspend fun saveExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    override suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }
}
