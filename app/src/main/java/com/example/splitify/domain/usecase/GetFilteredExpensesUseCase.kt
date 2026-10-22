package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFilteredExpensesUseCase(private val expenseRepository: ExpenseRepository) {
    operator fun invoke(category: String): Flow<List<Expense>> {
        return expenseRepository.getAllExpenses().map { list ->
            if (category == "All") {
                list
            } else {
                list.filter { it.category.equals(category, ignoreCase = true) }
            }
        }
    }
}
