package com.example.splitify.ui.friend_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.usecase.DeleteExpenseUseCase
import com.example.splitify.domain.usecase.GenerateSmsSummaryUseCase
import com.example.splitify.domain.usecase.GetFriendDetailsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FriendDetailViewModel(
    private val friendId: Long,
    getFriendDetailsUseCase: GetFriendDetailsUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val generateSmsSummaryUseCase: GenerateSmsSummaryUseCase
) : ViewModel() {

    val uiState: StateFlow<FriendDetailUiState> = getFriendDetailsUseCase(friendId).map { details ->
        FriendDetailUiState(
            friend = details.friend,
            expenses = details.expenses,
            totalOwed = details.totalOwed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendDetailUiState()
    )

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
        }
    }

    fun generateSmsSummary(): String {
        val currentState = uiState.value
        val friendName = currentState.friend?.name ?: "Friend"
        return generateSmsSummaryUseCase(friendName, currentState.expenses, currentState.totalOwed)
    }
}
