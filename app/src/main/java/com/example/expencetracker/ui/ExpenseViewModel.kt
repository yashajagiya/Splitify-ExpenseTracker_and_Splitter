package com.example.expencetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expencetracker.data.ExpenseEntity
import com.example.expencetracker.data.ExpenseRepository
import com.example.expencetracker.data.FriendEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.util.Locale

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AnalyticsTargetType { OVERALL, MY_SHARE, FRIEND }

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // App Theme State
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Category Filter for History Screen
    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Friend Profiles State
    val allFriends: StateFlow<List<FriendEntity>> = repository.allFriends.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        repository.allExpenses,
        _selectedCategoryFilter
    ) { list, category ->
        if (category == "All") {
            list
        } else {
            list.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSpent: StateFlow<Double> = repository.allExpenses.map { list ->
        list.sumOf { it.totalAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalPeopleInvolved: StateFlow<Int> = repository.allExpenses.map { list ->
        list.sumOf { it.peopleCount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Analytics Target State
    private val _analyticsTargetType = MutableStateFlow(AnalyticsTargetType.OVERALL)
    val analyticsTargetType: StateFlow<AnalyticsTargetType> = _analyticsTargetType.asStateFlow()

    private val _analyticsSelectedFriendId = MutableStateFlow<Long?>(null)
    val analyticsSelectedFriendId: StateFlow<Long?> = _analyticsSelectedFriendId.asStateFlow()

    val targetTotalSpent: StateFlow<Double> = combine(
        repository.allExpenses,
        _analyticsTargetType,
        _analyticsSelectedFriendId
    ) { list, targetType, friendId ->
        when (targetType) {
            AnalyticsTargetType.OVERALL -> list.sumOf { it.totalAmount }
            AnalyticsTargetType.MY_SHARE -> list.sumOf { exp ->
                if (exp.paidBy.equals("You", ignoreCase = true) || exp.friendId != null) {
                    exp.perPersonAmount
                } else {
                    exp.totalAmount
                }
            }
            AnalyticsTargetType.FRIEND -> {
                if (friendId != null) {
                    list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
                        .sumOf { it.perPersonAmount }
                } else 0.0
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val targetAverageExpense: StateFlow<Double> = combine(
        repository.allExpenses,
        _analyticsTargetType,
        _analyticsSelectedFriendId
    ) { list, targetType, friendId ->
        val filteredList = when (targetType) {
            AnalyticsTargetType.OVERALL, AnalyticsTargetType.MY_SHARE -> list
            AnalyticsTargetType.FRIEND -> list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
        }
        if (filteredList.isEmpty()) 0.0
        else {
            val total = if (targetType == AnalyticsTargetType.MY_SHARE) {
                filteredList.sumOf { it.perPersonAmount }
            } else {
                filteredList.sumOf { it.totalAmount }
            }
            total / filteredList.size
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val targetBiggestExpense: StateFlow<ExpenseEntity?> = combine(
        repository.allExpenses,
        _analyticsTargetType,
        _analyticsSelectedFriendId
    ) { list, targetType, friendId ->
        val filteredList = when (targetType) {
            AnalyticsTargetType.OVERALL, AnalyticsTargetType.MY_SHARE -> list
            AnalyticsTargetType.FRIEND -> list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
        }
        filteredList.maxByOrNull { it.totalAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val targetCategorySpendingMap: StateFlow<Map<String, Double>> = combine(
        repository.allExpenses,
        _analyticsTargetType,
        _analyticsSelectedFriendId
    ) { list, targetType, friendId ->
        val filteredList = when (targetType) {
            AnalyticsTargetType.OVERALL, AnalyticsTargetType.MY_SHARE -> list
            AnalyticsTargetType.FRIEND -> list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
        }
        filteredList.groupBy { it.category }.mapValues { entry ->
            if (targetType == AnalyticsTargetType.MY_SHARE) entry.value.sumOf { it.perPersonAmount }
            else entry.value.sumOf { it.totalAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val targetPaymentMethodSpendingMap: StateFlow<Map<String, Double>> = combine(
        repository.allExpenses,
        _analyticsTargetType,
        _analyticsSelectedFriendId
    ) { list, targetType, friendId ->
        val filteredList = when (targetType) {
            AnalyticsTargetType.OVERALL, AnalyticsTargetType.MY_SHARE -> list
            AnalyticsTargetType.FRIEND -> list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
        }
        filteredList.groupBy { it.paymentMethod }.mapValues { entry ->
            if (targetType == AnalyticsTargetType.MY_SHARE) entry.value.sumOf { it.perPersonAmount }
            else entry.value.sumOf { it.totalAmount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Form State for Add / Edit / Split Expense Screen
    private val _editingExpenseId = MutableStateFlow<Long?>(null)
    val editingExpenseId: StateFlow<Long?> = _editingExpenseId.asStateFlow()

    private val _titleInput = MutableStateFlow("")
    val titleInput: StateFlow<String> = _titleInput.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _peopleCountInput = MutableStateFlow(2)
    val peopleCountInput: StateFlow<Int> = _peopleCountInput.asStateFlow()

    private val _selectedFormCategory = MutableStateFlow("Food")
    val selectedFormCategory: StateFlow<String> = _selectedFormCategory.asStateFlow()

    // Multiple Friends Selection State
    private val _selectedFriendIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedFriendIds: StateFlow<Set<Long>> = _selectedFriendIds.asStateFlow()

    private val _selectedFriendNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val selectedFriendNames: StateFlow<Map<Long, String>> = _selectedFriendNames.asStateFlow()

    private val _paidByInput = MutableStateFlow("You")
    val paidByInput: StateFlow<String> = _paidByInput.asStateFlow()

    private val _paymentMethodInput = MutableStateFlow("UPI")
    val paymentMethodInput: StateFlow<String> = _paymentMethodInput.asStateFlow()

    private val _selectedDateTimestamp = MutableStateFlow(System.currentTimeMillis())
    val selectedDateTimestamp: StateFlow<Long> = _selectedDateTimestamp.asStateFlow()

    private val _titleError = MutableStateFlow<String?>(null)
    val titleError: StateFlow<String?> = _titleError.asStateFlow()

    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()

    private val _recentlyDeletedExpense = MutableStateFlow<ExpenseEntity?>(null)
    val recentlyDeletedExpense: StateFlow<ExpenseEntity?> = _recentlyDeletedExpense.asStateFlow()

    val perPersonShare: StateFlow<Double> = combine(_amountInput, _peopleCountInput) { amountStr, count ->
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val people = if (count > 0) count else 1
        amount / people
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
    }

    fun setAnalyticsTarget(type: AnalyticsTargetType, friendId: Long? = null) {
        _analyticsTargetType.value = type
        _analyticsSelectedFriendId.value = friendId
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun onTitleChange(newTitle: String) {
        _titleInput.value = newTitle
        if (_titleError.value != null && newTitle.isNotBlank()) {
            _titleError.value = null
        }
    }

    fun onAmountChange(newAmount: String) {
        val filtered = newAmount.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            _amountInput.value = filtered
            if (_amountError.value != null && (filtered.toDoubleOrNull() ?: 0.0) > 0.0) {
                _amountError.value = null
            }
        }
    }

    fun onPeopleCountChange(newCount: Int) {
        _peopleCountInput.value = newCount.coerceIn(1, 50)
    }

    fun onFormCategorySelect(category: String) {
        _selectedFormCategory.value = category
    }

    fun toggleFriendForExpense(friendId: Long, friendName: String) {
        val currentIds = _selectedFriendIds.value.toMutableSet()
        val currentNames = _selectedFriendNames.value.toMutableMap()

        if (currentIds.contains(friendId)) {
            currentIds.remove(friendId)
            currentNames.remove(friendId)
        } else {
            currentIds.add(friendId)
            currentNames[friendId] = friendName
        }

        _selectedFriendIds.value = currentIds
        _selectedFriendNames.value = currentNames

        // Automatically update split people count (Selected Friends + You)
        val newPeopleCount = if (currentIds.isNotEmpty()) currentIds.size + 1 else 2
        _peopleCountInput.value = newPeopleCount.coerceIn(1, 50)

        if (_paidByInput.value != "You" && !currentNames.values.contains(_paidByInput.value)) {
            _paidByInput.value = "You"
        }
    }

    fun clearFriendSelectionForExpense() {
        _selectedFriendIds.value = emptySet()
        _selectedFriendNames.value = emptyMap()
        _paidByInput.value = "You"
    }

    fun selectSingleFriendForExpense(friendId: Long, friendName: String) {
        _selectedFriendIds.value = setOf(friendId)
        _selectedFriendNames.value = mapOf(friendId to friendName)
        _peopleCountInput.value = 2
    }

    fun onPaidByChange(paidBy: String) {
        _paidByInput.value = paidBy
    }

    fun onPaymentMethodChange(method: String) {
        _paymentMethodInput.value = method
    }

    fun onDateChange(timestamp: Long) {
        _selectedDateTimestamp.value = timestamp
    }

    fun prepareEditExpense(expense: ExpenseEntity) {
        _editingExpenseId.value = expense.id
        _titleInput.value = expense.title
        _amountInput.value = if (expense.totalAmount % 1.0 == 0.0) {
            expense.totalAmount.toLong().toString()
        } else {
            expense.totalAmount.toString()
        }
        _peopleCountInput.value = expense.peopleCount
        _selectedFormCategory.value = expense.category

        if (!expense.friendIds.isNullOrBlank() && !expense.friendNames.isNullOrBlank()) {
            val ids = expense.friendIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
            val namesList = expense.friendNames.split(",").map { it.trim() }
            val namesMap = mutableMapOf<Long, String>()
            ids.zip(namesList).forEach { (id, name) -> namesMap[id] = name }
            _selectedFriendIds.value = ids
            _selectedFriendNames.value = namesMap
        } else if (expense.friendId != null && expense.friendName != null) {
            _selectedFriendIds.value = setOf(expense.friendId)
            _selectedFriendNames.value = mapOf(expense.friendId to expense.friendName)
        } else {
            _selectedFriendIds.value = emptySet()
            _selectedFriendNames.value = emptyMap()
        }

        _paidByInput.value = expense.paidBy
        _paymentMethodInput.value = expense.paymentMethod
        _selectedDateTimestamp.value = expense.timestamp
        _titleError.value = null
        _amountError.value = null
    }

    fun addFriendProfile(name: String, phoneNumber: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val friend = FriendEntity(
                name = name.trim(),
                phoneNumber = phoneNumber.trim()
            )
            repository.insertFriend(friend)
        }
    }

    fun deleteFriendProfile(friend: FriendEntity) {
        viewModelScope.launch {
            repository.deleteFriend(friend)
        }
    }

    fun getFriendById(friendId: Long): StateFlow<FriendEntity?> {
        return repository.getFriendById(friendId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun getExpensesForFriend(friendId: Long): StateFlow<List<ExpenseEntity>> {
        return repository.getExpensesForFriend(friendId).map { list ->
            list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun getFriendTotalOwed(friendId: Long): StateFlow<Double> {
        return repository.getExpensesForFriend(friendId).map { list ->
            list.filter { exp -> exp.friendId == friendId || exp.friendIds?.split(",")?.contains(friendId.toString()) == true }
                .sumOf { expense ->
                    if (expense.paidBy.equals("You", ignoreCase = true)) {
                        expense.perPersonAmount
                    } else {
                        -expense.perPersonAmount
                    }
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    }

    fun saveExpense(): Boolean {
        val title = _titleInput.value.trim()
        val amount = _amountInput.value.toDoubleOrNull() ?: 0.0
        val count = _peopleCountInput.value
        val category = _selectedFormCategory.value

        val selectedIds = _selectedFriendIds.value
        val selectedNamesMap = _selectedFriendNames.value

        val firstFriendId = selectedIds.firstOrNull()
        val firstFriendName = if (firstFriendId != null) selectedNamesMap[firstFriendId] else null

        val friendIdsStr = if (selectedIds.isNotEmpty()) selectedIds.joinToString(",") else null
        val friendNamesStr = if (selectedNamesMap.isNotEmpty()) selectedNamesMap.values.joinToString(",") else null

        val paidBy = _paidByInput.value
        val paymentMethod = _paymentMethodInput.value
        val dateTimestamp = _selectedDateTimestamp.value
        val currentEditId = _editingExpenseId.value

        var isValid = true

        if (title.isBlank()) {
            _titleError.value = "Title cannot be empty"
            isValid = false
        } else {
            _titleError.value = null
        }

        if (amount <= 0.0) {
            _amountError.value = "Enter a valid amount > 0"
            isValid = false
        } else {
            _amountError.value = null
        }

        if (!isValid) return false

        val perPerson = amount / count

        val expense = ExpenseEntity(
            id = currentEditId ?: 0,
            title = title,
            category = category,
            totalAmount = amount,
            peopleCount = count,
            perPersonAmount = perPerson,
            timestamp = dateTimestamp,
            friendId = firstFriendId,
            friendName = firstFriendName,
            friendIds = friendIdsStr,
            friendNames = friendNamesStr,
            paidBy = paidBy,
            paymentMethod = paymentMethod
        )

        viewModelScope.launch {
            repository.insertExpense(expense)
            resetForm()
        }

        return true
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            _recentlyDeletedExpense.value = expense
            repository.deleteExpense(expense)
        }
    }

    fun undoDelete() {
        val lastDeleted = _recentlyDeletedExpense.value
        if (lastDeleted != null) {
            viewModelScope.launch {
                repository.insertExpense(lastDeleted)
                _recentlyDeletedExpense.value = null
            }
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeletedExpense.value = null
    }

    fun resetForm() {
        _editingExpenseId.value = null
        _titleInput.value = ""
        _amountInput.value = ""
        _peopleCountInput.value = 2
        _selectedFormCategory.value = "Food"
        _selectedFriendIds.value = emptySet()
        _selectedFriendNames.value = emptyMap()
        _paidByInput.value = "You"
        _paymentMethodInput.value = "UPI"
        _selectedDateTimestamp.value = System.currentTimeMillis()
        _titleError.value = null
        _amountError.value = null
    }

    fun generateShareText(title: String, amount: Double, count: Int, perPerson: Double): String {
        val displayTitle = title.ifBlank { "Expense" }
        return "Split for $displayTitle: Total ₹${String.format(Locale.getDefault(), "%.2f", amount)} between $count people -> ₹${String.format(Locale.getDefault(), "%.2f", perPerson)} each"
    }

    fun generateSmsSummary(friendName: String, expenses: List<ExpenseEntity>, totalOwed: Double): String {
        val builder = StringBuilder()
        builder.append("Hi $friendName, here is your expense balance summary from SplitFlow:\n\n")

        if (expenses.isEmpty()) {
            builder.append("No active expenses linked to your profile.\n")
        } else {
            expenses.forEachIndexed { index, exp ->
                val payerText = if (exp.paidBy.equals("You", ignoreCase = true)) "Paid by You" else "Paid by ${exp.paidBy}"
                builder.append("${index + 1}. ${exp.title} ($payerText via ${exp.paymentMethod}): ₹${String.format(Locale.getDefault(), "%.2f", exp.perPersonAmount)} share\n")
            }
            builder.append("\n------------------------\n")
            if (totalOwed >= 0) {
                builder.append("TOTAL AMOUNT TO PAY: ₹${String.format(Locale.getDefault(), "%.2f", totalOwed)}\n")
            } else {
                builder.append("TOTAL AMOUNT YOU ARE OWED: ₹${String.format(Locale.getDefault(), "%.2f", -totalOwed)}\n")
            }
        }

        builder.append("\nPlease settle the balance when possible. Thank you!")
        return builder.toString()
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
