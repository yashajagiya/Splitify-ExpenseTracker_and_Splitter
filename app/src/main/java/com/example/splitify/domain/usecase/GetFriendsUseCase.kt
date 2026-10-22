package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.repository.FriendRepository
import kotlinx.coroutines.flow.Flow

class GetFriendsUseCase(private val friendRepository: FriendRepository) {
    operator fun invoke(): Flow<List<Friend>> = friendRepository.getAllFriends()
}
