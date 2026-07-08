package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val sessionManager: SessionManager
    val transactions: StateFlow<List<TransactionEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        sessionManager = SessionManager.getInstance(application)

        transactions = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) repository.getTransactions(email) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addTransaction(amount: Double, category: String, note: String, type: String, date: Long) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val transaction = TransactionEntity(
                userEmail = email,
                amount = amount,
                category = category,
                note = note,
                type = type,
                date = date
            )
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(id: Long, amount: Double, category: String, note: String, type: String, date: Long) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = id,
                userEmail = email,
                amount = amount,
                category = category,
                note = note,
                type = type,
                date = date
            )
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
