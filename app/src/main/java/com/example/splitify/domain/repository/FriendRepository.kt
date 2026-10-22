package com.example.splitify.domain.repository

import com.example.splitify.domain.model.Friend
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun getAllFriends(): Flow<List<Friend>>
    fun getFriendById(id: Long): Flow<Friend?>
    suspend fun saveFriend(friend: Friend): Long
    suspend fun updateFriend(friend: Friend)
    suspend fun deleteFriend(friend: Friend)
    suspend fun deleteFriendById(id: Long)
}
