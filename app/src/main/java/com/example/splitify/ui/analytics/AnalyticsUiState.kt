package com.example.splitify.ui.analytics

import com.example.splitify.domain.model.AnalyticsSummary
import com.example.splitify.domain.model.AnalyticsTargetType
import com.example.splitify.domain.model.Friend

data class AnalyticsUiState(
    val targetType: AnalyticsTargetType = AnalyticsTargetType.OVERALL,
    val selectedFriendId: Long? = null,
    val friendsList: List<Friend> = emptyList(),
    val summary: AnalyticsSummary = AnalyticsSummary()
)
