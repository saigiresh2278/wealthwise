package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.repository.GoalRepository
import com.example.wealthwiseai.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val goalRepository: GoalRepository
    private val userRepository: UserRepository
    private val sessionManager: SessionManager
    val goals: StateFlow<List<GoalEntity>>
    val userProfile: StateFlow<UserProfileEntity?>

    init {
        val database = AppDatabase.getDatabase(application)
        goalRepository = GoalRepository(database.goalDao())
        userRepository = UserRepository(database.userProfileDao())
        sessionManager = SessionManager.getInstance(application)
        
        goals = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) goalRepository.getGoals(email) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userProfile = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) userRepository.getUserProfile(email) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun addGoal(name: String, targetAmount: Double, currentSavedAmount: Double, targetDate: Long, priority: String) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val goal = GoalEntity(
                userEmail = email,
                goalName = name,
                targetAmount = targetAmount,
                currentSavedAmount = currentSavedAmount,
                targetDate = targetDate,
                priority = priority
            )
            goalRepository.insertGoal(goal)
        }
    }

    fun updateGoal(id: Long, name: String, targetAmount: Double, currentSavedAmount: Double, targetDate: Long, priority: String) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val goal = GoalEntity(
                id = id,
                userEmail = email,
                goalName = name,
                targetAmount = targetAmount,
                currentSavedAmount = currentSavedAmount,
                targetDate = targetDate,
                priority = priority
            )
            goalRepository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }
}
