package com.example.splitify.domain.model

enum class AnalyticsTargetType {
    OVERALL,
    MY_SHARE,
    FRIEND
}

data class AnalyticsSummary(
    val totalSpent: Double = 0.0,
    val averageExpense: Double = 0.0,
    val biggestExpense: Expense? = null,
    val categorySpendingMap: Map<String, Double> = emptyMap(),
    val paymentMethodSpendingMap: Map<String, Double> = emptyMap()
)
