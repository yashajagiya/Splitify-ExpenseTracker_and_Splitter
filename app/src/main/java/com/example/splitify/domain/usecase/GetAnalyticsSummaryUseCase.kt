package com.example.splitify.domain.usecase

import com.example.splitify.domain.model.AnalyticsSummary
import com.example.splitify.domain.model.AnalyticsTargetType
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAnalyticsSummaryUseCase(private val expenseRepository: ExpenseRepository) {
    operator fun invoke(
        targetType: AnalyticsTargetType,
        friendId: Long? = null
    ): Flow<AnalyticsSummary> {
        return expenseRepository.getAllExpenses().map { list ->
            val filteredList = when (targetType) {
                AnalyticsTargetType.OVERALL, AnalyticsTargetType.MY_SHARE -> list
                AnalyticsTargetType.FRIEND -> {
                    if (friendId != null) {
                        list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
                    } else emptyList()
                }
            }

            val totalSpent = when (targetType) {
                AnalyticsTargetType.OVERALL -> filteredList.sumOf { it.totalAmount }
                AnalyticsTargetType.MY_SHARE -> filteredList.sumOf { exp ->
                    if (exp.paidBy.equals("You", ignoreCase = true) || exp.friendId != null) {
                        exp.perPersonAmount
                    } else {
                        exp.totalAmount
                    }
                }
                AnalyticsTargetType.FRIEND -> filteredList.sumOf { it.perPersonAmount }
            }

            val averageExpense = if (filteredList.isEmpty()) 0.0 else {
                val sumForAvg = if (targetType == AnalyticsTargetType.MY_SHARE) {
                    filteredList.sumOf { it.perPersonAmount }
                } else {
                    filteredList.sumOf { it.totalAmount }
                }
                sumForAvg / filteredList.size
            }

            val biggestExpense = filteredList.maxByOrNull { it.totalAmount }

            val categoryMap = filteredList.groupBy { it.category }.mapValues { entry ->
                if (targetType == AnalyticsTargetType.MY_SHARE) entry.value.sumOf { it.perPersonAmount }
                else entry.value.sumOf { it.totalAmount }
            }

            val paymentMethodMap = filteredList.groupBy { it.paymentMethod }.mapValues { entry ->
                if (targetType == AnalyticsTargetType.MY_SHARE) entry.value.sumOf { it.perPersonAmount }
                else entry.value.sumOf { it.totalAmount }
            }

            AnalyticsSummary(
                totalSpent = totalSpent,
                averageExpense = averageExpense,
                biggestExpense = biggestExpense,
                categorySpendingMap = categoryMap,
                paymentMethodSpendingMap = paymentMethodMap
            )
        }
    }
}
