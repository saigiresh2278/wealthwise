package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.ai.FinancialHealthCalculator
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.data.repository.UserRepository
import com.example.wealthwiseai.data.repository.TransactionRepository
import com.example.wealthwiseai.data.repository.GoalRepository
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val transactionRepository: TransactionRepository
    private val goalRepository: GoalRepository
    private val sessionManager: SessionManager

    val userProfile: StateFlow<UserProfileEntity?>
    val transactions: StateFlow<List<TransactionEntity>>
    val goals: StateFlow<List<GoalEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        userRepository = UserRepository(database.userProfileDao())
        transactionRepository = TransactionRepository(database.transactionDao())
        goalRepository = GoalRepository(database.goalDao())
        sessionManager = SessionManager.getInstance(application)

        userProfile = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) userRepository.getUserProfile(email) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        transactions = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) transactionRepository.getTransactions(email) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        goals = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) goalRepository.getGoals(email) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val dynamicMonthlyIncome: StateFlow<Double> = userProfile.map { profile ->
        profile?.monthlyIncome ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dynamicMonthlyExpenses: StateFlow<Double> = transactions.map { txs ->
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val txCalendar = Calendar.getInstance()

        txs.filter { tx ->
            if (tx.type.lowercase() == "expense") {
                txCalendar.timeInMillis = tx.date
                txCalendar.get(Calendar.MONTH) == currentMonth && txCalendar.get(Calendar.YEAR) == currentYear
            } else {
                false
            }
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dynamicMonthlySavings: StateFlow<Double> = combine(dynamicMonthlyIncome, dynamicMonthlyExpenses) { income, expenses ->
        (income - expenses).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val savingsRate: StateFlow<Double> = combine(dynamicMonthlyIncome, dynamicMonthlyExpenses) { income, expenses ->
        FinancialHealthCalculator.calculateSavingsRate(income, expenses)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val financialHealthScore: StateFlow<Int> = combine(dynamicMonthlyIncome, dynamicMonthlyExpenses, dynamicMonthlySavings) { income, expenses, savings ->
        FinancialHealthCalculator.calculateHealthScore(
            income = income,
            expenses = expenses,
            currentSavedAmountForEmergency = savings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentTransactions: Flow<List<TransactionEntity>> = transactions.map { txs ->
        txs.take(5)
    }
}
