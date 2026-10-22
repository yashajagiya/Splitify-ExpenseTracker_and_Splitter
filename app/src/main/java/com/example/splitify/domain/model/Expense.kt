package com.example.splitify.domain.model

data class Expense(
    val id: Long = 0,
    val title: String,
    val category: String,
    val totalAmount: Double,
    val peopleCount: Int,
    val perPersonAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val friendId: Long? = null,
    val friendName: String? = null,
    val friendIds: String? = null,
    val friendNames: String? = null,
    val paidBy: String = "You",
    val paymentMethod: String = "UPI"
)
