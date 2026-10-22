package com.example.splitify.data.repository

import com.example.splitify.data.FriendDao
import com.example.splitify.data.mapper.toDomain
import com.example.splitify.data.mapper.toEntity
import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.repository.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FriendRepositoryImpl(
    private val friendDao: FriendDao
) : FriendRepository {

    override fun getAllFriends(): Flow<List<Friend>> {
        return friendDao.getAllFriends().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFriendById(id: Long): Flow<Friend?> {
        return friendDao.getFriendById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveFriend(friend: Friend): Long {
        return friendDao.insertFriend(friend.toEntity())
    }

    override suspend fun updateFriend(friend: Friend) {
        friendDao.updateFriend(friend.toEntity())
    }

    override suspend fun deleteFriend(friend: Friend) {
        friendDao.deleteFriend(friend.toEntity())
    }

    override suspend fun deleteFriendById(id: Long) {
        friendDao.deleteFriendById(id)
    }
}
