package com.example.splitify.ui.add_expense

import com.example.splitify.domain.model.Friend

data class AddExpenseUiState(
    val editingExpenseId: Long? = null,
    val titleInput: String = "",
    val amountInput: String = "",
    val peopleCountInput: Int = 2,
    val selectedCategory: String = "Food",
    val selectedFriendIds: Set<Long> = emptySet(),
    val selectedFriendNames: Map<Long, String> = emptyMap(),
    val availableFriends: List<Friend> = emptyList(),
    val paidByInput: String = "You",
    val paymentMethodInput: String = "UPI",
    val selectedDateTimestamp: Long = System.currentTimeMillis(),
    val titleError: String? = null,
    val amountError: String? = null,
    val perPersonShare: Double = 0.0
)
