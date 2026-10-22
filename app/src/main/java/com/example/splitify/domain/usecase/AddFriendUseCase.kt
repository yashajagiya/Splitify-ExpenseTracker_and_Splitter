package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.repository.FriendRepository

class AddFriendUseCase(private val friendRepository: FriendRepository) {
    suspend operator fun invoke(name: String, phoneNumber: String): Long {
        if (name.isBlank()) return -1
        val friend = Friend(
            name = name.trim(),
            phoneNumber = phoneNumber.trim()
        )
        return friendRepository.saveFriend(friend)
    }
}
