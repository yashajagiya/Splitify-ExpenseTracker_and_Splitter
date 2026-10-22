package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetExpensesUseCase(private val expenseRepository: ExpenseRepository) {
    operator fun invoke(): Flow<List<Expense>> = expenseRepository.getAllExpenses()
}
