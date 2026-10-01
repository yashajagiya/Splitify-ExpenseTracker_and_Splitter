package com.example.expencetracker

import android.app.Application
import com.example.expencetracker.data.ExpenseDatabase
import com.example.expencetracker.data.ExpenseRepository

class SplitFlowApplication : Application() {
    val database: ExpenseDatabase by lazy { ExpenseDatabase.getDatabase(this) }
    val repository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao(), database.friendDao())
    }
}
