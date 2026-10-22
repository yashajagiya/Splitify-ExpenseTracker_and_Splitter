package com.example.splitify.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.AnalyticsTargetType
import com.example.splitify.domain.usecase.GetAnalyticsSummaryUseCase
import com.example.splitify.domain.usecase.GetFriendsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    getFriendsUseCase: GetFriendsUseCase,
    private val getAnalyticsSummaryUseCase: GetAnalyticsSummaryUseCase
) : ViewModel() {

    private val _targetType = MutableStateFlow(AnalyticsTargetType.OVERALL)
    private val _selectedFriendId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        getFriendsUseCase(),
        _targetType,
        _selectedFriendId,
        combine(_targetType, _selectedFriendId) { type, fId -> Pair(type, fId) }
            .flatMapLatest { (type, fId) -> getAnalyticsSummaryUseCase(type, fId) }
    ) { friends, type, friendId, summary ->
        AnalyticsUiState(
            targetType = type,
            selectedFriendId = friendId,
            friendsList = friends,
            summary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun setTargetType(type: AnalyticsTargetType, friendId: Long? = null) {
        _targetType.value = type
        _selectedFriendId.value = friendId
    }
}
