package com.example.splitify.ui.friends

import com.example.splitify.domain.model.Friend

data class FriendsUiState(
    val friends: List<Friend> = emptyList()
)
