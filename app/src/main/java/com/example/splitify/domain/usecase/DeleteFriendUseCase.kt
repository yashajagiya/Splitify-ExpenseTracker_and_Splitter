package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.repository.FriendRepository

class DeleteFriendUseCase(private val friendRepository: FriendRepository) {
    suspend operator fun invoke(friend: Friend) {
        friendRepository.deleteFriend(friend)
    }
}
