package com.example.splitify.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.usecase.DeleteExpenseUseCase
import com.example.splitify.domain.usecase.GetFilteredExpensesUseCase
import com.example.splitify.domain.usecase.SaveExpenseUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getFilteredExpensesUseCase: GetFilteredExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase
) : ViewModel() {

    private val _selectedCategoryFilter = MutableStateFlow("All")
    private val _recentlyDeletedExpense = MutableStateFlow<Expense?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedCategoryFilter.flatMapLatest { category -> getFilteredExpensesUseCase(category) },
        _selectedCategoryFilter,
        _recentlyDeletedExpense
    ) { filteredList, category, recentlyDeleted ->
        val totalSpent = filteredList.sumOf { it.totalAmount }
        val totalPeople = filteredList.sumOf { it.peopleCount }
        HistoryUiState(
            selectedCategoryFilter = category,
            expenses = filteredList,
            totalSpent = totalSpent,
            totalPeopleInvolved = totalPeople,
            recentlyDeletedExpense = recentlyDeleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            _recentlyDeletedExpense.value = expense
            deleteExpenseUseCase(expense)
        }
    }

    fun undoDelete() {
        val lastDeleted = _recentlyDeletedExpense.value
        if (lastDeleted != null) {
            viewModelScope.launch {
                saveExpenseUseCase(lastDeleted)
                _recentlyDeletedExpense.value = null
            }
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeletedExpense.value = null
    }
}
