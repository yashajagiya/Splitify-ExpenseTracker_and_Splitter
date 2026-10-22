package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(private val expenseRepository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense) {
        expenseRepository.deleteExpense(expense)
    }
}
