package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository
    private val sessionManager: SessionManager
    val userProfile: StateFlow<UserProfileEntity?>

    init {
        val database = AppDatabase.getDatabase(application)
        userRepository = UserRepository(database.userProfileDao())
        sessionManager = SessionManager.getInstance(application)

        userProfile = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) {
                userRepository.getUserProfile(email)
            } else {
                flowOf(null)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun saveProfile(
        name: String,
        age: Int,
        occupation: String,
        monthlyIncome: Double,
        monthlyExpenses: Double,
        monthlySavings: Double,
        mainFinancialGoal: String,
        riskComfort: String,
        investmentExperience: String
    ) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val profile = UserProfileEntity(
                email = email,
                fullName = name,
                age = age,
                occupation = occupation,
                monthlyIncome = monthlyIncome,
                monthlyExpenses = monthlyExpenses,
                monthlySavings = monthlySavings,
                mainFinancialGoal = mainFinancialGoal,
                riskComfort = riskComfort,
                investmentExperience = investmentExperience
            )
            userRepository.insertUserProfile(profile)
        }
    }
}
