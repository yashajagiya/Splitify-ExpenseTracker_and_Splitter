package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class FriendDetails(
    val friend: Friend?,
    val expenses: List<Expense>,
    val totalOwed: Double
)

class GetFriendDetailsUseCase(
    private val friendRepository: FriendRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(friendId: Long): Flow<FriendDetails> {
        return combine(
            friendRepository.getFriendById(friendId),
            expenseRepository.getExpensesForFriend(friendId)
        ) { friend, allFriendExpenses ->
            val linkedExpenses = allFriendExpenses.filter { exp ->
                exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true
            }
            val totalOwed = linkedExpenses.sumOf { expense ->
                if (expense.paidBy.equals("You", ignoreCase = true)) {
                    expense.perPersonAmount
                } else {
                    -expense.perPersonAmount
                }
            }
            FriendDetails(
                friend = friend,
                expenses = linkedExpenses,
                totalOwed = totalOwed
            )
        }
    }
}
