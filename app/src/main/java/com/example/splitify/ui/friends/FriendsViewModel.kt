package com.example.splitify.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.usecase.AddFriendUseCase
import com.example.splitify.domain.usecase.DeleteFriendUseCase
import com.example.splitify.domain.usecase.GetFriendsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FriendsViewModel(
    getFriendsUseCase: GetFriendsUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase
) : ViewModel() {

    val uiState: StateFlow<FriendsUiState> = getFriendsUseCase().map { list ->
        FriendsUiState(friends = list)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FriendsUiState()
    )

    fun addFriend(name: String, phoneNumber: String) {
        viewModelScope.launch {
            addFriendUseCase(name, phoneNumber)
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            deleteFriendUseCase(friend)
        }
    }
}
