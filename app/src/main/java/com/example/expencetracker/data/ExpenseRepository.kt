package com.example.expencetracker.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val friendDao: FriendDao
) {

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allFriends: Flow<List<FriendEntity>> = friendDao.getAllFriends()

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? {
        return expenseDao.getExpenseById(id)
    }

    fun getExpensesForFriend(friendId: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForFriend(friendId)
    }

    fun getTotalOwedForFriend(friendId: Long): Flow<Double?> {
        return expenseDao.getTotalOwedForFriend(friendId)
    }

    fun getFriendById(id: Long): Flow<FriendEntity?> {
        return friendDao.getFriendById(id)
    }

    suspend fun insertFriend(friend: FriendEntity): Long {
        return friendDao.insertFriend(friend)
    }

    suspend fun updateFriend(friend: FriendEntity) {
        friendDao.updateFriend(friend)
    }

    suspend fun deleteFriend(friend: FriendEntity) {
        friendDao.deleteFriend(friend)
    }
}
