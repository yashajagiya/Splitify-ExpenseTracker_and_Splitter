package com.example.splitify.ui.friend_detail

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Friend

data class FriendDetailUiState(
    val friend: Friend? = null,
    val expenses: List<Expense> = emptyList(),
    val totalOwed: Double = 0.0
)
