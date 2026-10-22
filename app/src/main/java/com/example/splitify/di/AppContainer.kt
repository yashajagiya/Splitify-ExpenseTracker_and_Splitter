package com.example.splitify.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.splitify.data.ExpenseDatabase
import com.example.splitify.data.repository.ExpenseRepositoryImpl
import com.example.splitify.data.repository.FriendRepositoryImpl
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.FriendRepository
import com.example.splitify.domain.usecase.AddFriendUseCase
import com.example.splitify.domain.usecase.DeleteExpenseUseCase
import com.example.splitify.domain.usecase.DeleteFriendUseCase
import com.example.splitify.domain.usecase.GenerateShareTextUseCase
import com.example.splitify.domain.usecase.GenerateSmsSummaryUseCase
import com.example.splitify.domain.usecase.GetAnalyticsSummaryUseCase
import com.example.splitify.domain.usecase.GetExpensesUseCase
import com.example.splitify.domain.usecase.GetFilteredExpensesUseCase
import com.example.splitify.domain.usecase.GetFriendDetailsUseCase
import com.example.splitify.domain.usecase.GetFriendsUseCase
import com.example.splitify.domain.usecase.SaveExpenseUseCase
import com.example.splitify.ui.add_expense.AddExpenseViewModel
import com.example.splitify.ui.analytics.AnalyticsViewModel
import com.example.splitify.ui.friend_detail.FriendDetailViewModel
import com.example.splitify.ui.friends.FriendsViewModel
import com.example.splitify.ui.history.HistoryViewModel
import com.example.splitify.ui.theme.ThemeViewModel

class AppContainer(private val context: Context) {

    private val database: ExpenseDatabase by lazy {
        ExpenseDatabase.getDatabase(context)
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }

    val friendRepository: FriendRepository by lazy {
        FriendRepositoryImpl(database.friendDao())
    }

    // Use Cases
    val getExpensesUseCase: GetExpensesUseCase by lazy {
        GetExpensesUseCase(expenseRepository)
    }

    val getFilteredExpensesUseCase: GetFilteredExpensesUseCase by lazy {
        GetFilteredExpensesUseCase(expenseRepository)
    }

    val saveExpenseUseCase: SaveExpenseUseCase by lazy {
        SaveExpenseUseCase(expenseRepository)
    }

    val deleteExpenseUseCase: DeleteExpenseUseCase by lazy {
        DeleteExpenseUseCase(expenseRepository)
    }

    val getFriendsUseCase: GetFriendsUseCase by lazy {
        GetFriendsUseCase(friendRepository)
    }

    val getFriendDetailsUseCase: GetFriendDetailsUseCase by lazy {
        GetFriendDetailsUseCase(friendRepository, expenseRepository)
    }

    val addFriendUseCase: AddFriendUseCase by lazy {
        AddFriendUseCase(friendRepository)
    }

    val deleteFriendUseCase: DeleteFriendUseCase by lazy {
        DeleteFriendUseCase(friendRepository)
    }

    val getAnalyticsSummaryUseCase: GetAnalyticsSummaryUseCase by lazy {
        GetAnalyticsSummaryUseCase(expenseRepository)
    }

    val generateSmsSummaryUseCase: GenerateSmsSummaryUseCase by lazy {
        GenerateSmsSummaryUseCase()
    }

    val generateShareTextUseCase: GenerateShareTextUseCase by lazy {
        GenerateShareTextUseCase()
    }

    // ViewModel Factories
    val themeViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { ThemeViewModel() }
    }

    val historyViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { HistoryViewModel(getFilteredExpensesUseCase, deleteExpenseUseCase, saveExpenseUseCase) }
    }

    val addExpenseViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { AddExpenseViewModel(getFriendsUseCase, saveExpenseUseCase, generateShareTextUseCase) }
    }

    val friendsViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { FriendsViewModel(getFriendsUseCase, addFriendUseCase, deleteFriendUseCase) }
    }

    val friendDetailViewModelFactory: (Long) -> ViewModelProvider.Factory = { friendId ->
        viewModelFactory {
            initializer { FriendDetailViewModel(friendId, getFriendDetailsUseCase, deleteExpenseUseCase, generateSmsSummaryUseCase) }
        }
    }

    val analyticsViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { AnalyticsViewModel(getFriendsUseCase, getAnalyticsSummaryUseCase) }
    }
}
