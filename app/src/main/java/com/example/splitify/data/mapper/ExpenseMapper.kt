package com.example.splitify.data.mapper

import com.example.splitify.data.ExpenseEntity
import com.example.splitify.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        title = title,
        category = category,
        totalAmount = totalAmount,
        peopleCount = peopleCount,
        perPersonAmount = perPersonAmount,
        timestamp = timestamp,
        friendId = friendId,
        friendName = friendName,
        friendIds = friendIds,
        friendNames = friendNames,
        paidBy = paidBy,
        paymentMethod = paymentMethod
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        title = title,
        category = category,
        totalAmount = totalAmount,
        peopleCount = peopleCount,
        perPersonAmount = perPersonAmount,
        timestamp = timestamp,
        friendId = friendId,
        friendName = friendName,
        friendIds = friendIds,
        friendNames = friendNames,
        paidBy = paidBy,
        paymentMethod = paymentMethod
    )
}
