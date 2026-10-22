package com.example.splitify.data.mapper

import com.example.splitify.data.FriendEntity
import com.example.splitify.domain.model.Friend

fun FriendEntity.toDomain(): Friend {
    return Friend(
        id = id,
        name = name,
        phoneNumber = phoneNumber
    )
}

fun Friend.toEntity(): FriendEntity {
    return FriendEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber
    )
}
