package com.example.splitify.ui.history

import com.example.splitify.domain.model.Expense

data class HistoryUiState(
    val selectedCategoryFilter: String = "All",
    val expenses: List<Expense> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalPeopleInvolved: Int = 0,
    val recentlyDeletedExpense: Expense? = null
)
