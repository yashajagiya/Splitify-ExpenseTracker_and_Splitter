package com.example.splitify.ui.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.model.Expense
import com.example.splitify.domain.model.Friend
import com.example.splitify.domain.usecase.GenerateShareTextUseCase
import com.example.splitify.domain.usecase.GetFriendsUseCase
import com.example.splitify.domain.usecase.SaveExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    getFriendsUseCase: GetFriendsUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val generateShareTextUseCase: GenerateShareTextUseCase
) : ViewModel() {

    private val _editingExpenseId = MutableStateFlow<Long?>(null)
    private val _titleInput = MutableStateFlow("")
    private val _amountInput = MutableStateFlow("")
    private val _peopleCountInput = MutableStateFlow(2)
    private val _selectedCategory = MutableStateFlow("Food")
    private val _selectedFriendIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedFriendNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    private val _paidByInput = MutableStateFlow("You")
    private val _paymentMethodInput = MutableStateFlow("UPI")
    private val _selectedDateTimestamp = MutableStateFlow(System.currentTimeMillis())
    private val _titleError = MutableStateFlow<String?>(null)
    private val _amountError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AddExpenseUiState> = combine(
        getFriendsUseCase(),
        _editingExpenseId,
        _titleInput,
        _amountInput,
        _peopleCountInput,
        _selectedCategory,
        _selectedFriendIds,
        _selectedFriendNames,
        _paidByInput,
        _paymentMethodInput,
        _selectedDateTimestamp,
        _titleError,
        _amountError
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val friendsList = args[0] as List<Friend>
        val editingId = args[1] as Long?
        val title = args[2] as String
        val amountStr = args[3] as String
        val peopleCount = args[4] as Int
        val category = args[5] as String
        @Suppress("UNCHECKED_CAST")
        val friendIds = args[6] as Set<Long>
        @Suppress("UNCHECKED_CAST")
        val friendNames = args[7] as Map<Long, String>
        val paidBy = args[8] as String
        val paymentMethod = args[9] as String
        val dateTimestamp = args[10] as Long
        val tError = args[11] as String?
        val aError = args[12] as String?

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val count = if (peopleCount > 0) peopleCount else 1
        val perPerson = amount / count

        AddExpenseUiState(
            editingExpenseId = editingId,
            titleInput = title,
            amountInput = amountStr,
            peopleCountInput = peopleCount,
            selectedCategory = category,
            selectedFriendIds = friendIds,
            selectedFriendNames = friendNames,
            availableFriends = friendsList,
            paidByInput = paidBy,
            paymentMethodInput = paymentMethod,
            selectedDateTimestamp = dateTimestamp,
            titleError = tError,
            amountError = aError,
            perPersonShare = perPerson
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState()
    )

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

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
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

        val newPeopleCount = if (currentIds.isNotEmpty()) currentIds.size + 1 else 2
        _peopleCountInput.value = newPeopleCount.coerceIn(1, 50)

        if (_paidByInput.value != "You" && !currentNames.values.contains(_paidByInput.value)) {
            _paidByInput.value = "You"
        }
    }

    fun selectSingleFriendForExpense(friendId: Long, friendName: String) {
        _selectedFriendIds.value = setOf(friendId)
        _selectedFriendNames.value = mapOf(friendId to friendName)
        _peopleCountInput.value = 2
    }

    fun clearFriendSelection() {
        _selectedFriendIds.value = emptySet()
        _selectedFriendNames.value = emptyMap()
        _paidByInput.value = "You"
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

    fun prepareEditExpense(expense: Expense) {
        _editingExpenseId.value = expense.id
        _titleInput.value = expense.title
        _amountInput.value = if (expense.totalAmount % 1.0 == 0.0) {
            expense.totalAmount.toLong().toString()
        } else {
            expense.totalAmount.toString()
        }
        _peopleCountInput.value = expense.peopleCount
        _selectedCategory.value = expense.category

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

    fun saveExpense(): Boolean {
        val title = _titleInput.value.trim()
        val amount = _amountInput.value.toDoubleOrNull() ?: 0.0
        val count = _peopleCountInput.value
        val category = _selectedCategory.value

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

        val expense = Expense(
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
            saveExpenseUseCase(expense)
            resetForm()
        }

        return true
    }

    fun resetForm() {
        _editingExpenseId.value = null
        _titleInput.value = ""
        _amountInput.value = ""
        _peopleCountInput.value = 2
        _selectedCategory.value = "Food"
        _selectedFriendIds.value = emptySet()
        _selectedFriendNames.value = emptyMap()
        _paidByInput.value = "You"
        _paymentMethodInput.value = "UPI"
        _selectedDateTimestamp.value = System.currentTimeMillis()
        _titleError.value = null
        _amountError.value = null
    }

    fun generateShareText(title: String, amount: Double, count: Int, perPerson: Double): String {
        return generateShareTextUseCase(title, amount, count, perPerson)
    }
}
