package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.entity.*
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DatabaseExplorerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    val users: StateFlow<List<AuthEntity>> = database.authDao().getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val profiles: StateFlow<List<UserProfileEntity>> = database.userProfileDao().getAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<TransactionEntity>> = database.transactionDao().getAllTransactionsAdmin()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val goals: StateFlow<List<GoalEntity>> = database.goalDao().getAllGoalsAdmin()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val riskProfiles: StateFlow<List<RiskProfileEntity>> = database.riskProfileDao().getAllRiskProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteUser(user: AuthEntity) {
        viewModelScope.launch {
            database.authDao().deleteUser(user)
            FirebaseSyncHelper.clearUserData(user.email)
            
            // Clean up other local tables associated with this user
            val profile = database.userProfileDao().getProfileByEmailDirect(user.email)
            if (profile != null) {
                database.userProfileDao().deleteUserProfile(profile)
            }
            // For simplicity, we trigger deletion of user's local transactions and goals
            // inside transaction and goal repositories or direct calls
            // Since they are cleared from Firestore, we can also clear them locally
            // directly or keep it simple. Let's delete them locally as well:
            val localTxns = database.transactionDao().getAllTransactionsAdmin().stateIn(viewModelScope).value
                .filter { it.userEmail == user.email }
            localTxns.forEach { database.transactionDao().deleteTransaction(it) }

            val localGoals = database.goalDao().getAllGoalsAdmin().stateIn(viewModelScope).value
                .filter { it.userEmail == user.email }
            localGoals.forEach { database.goalDao().deleteGoal(it) }

            val localRisk = database.riskProfileDao().getAllRiskProfiles().stateIn(viewModelScope).value
                .find { it.email == user.email }
            if (localRisk != null) {
                database.riskProfileDao().deleteRiskProfile(localRisk)
            }
        }
    }

    fun deleteProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            database.userProfileDao().deleteUserProfile(profile)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            database.transactionDao().deleteTransaction(transaction)
            FirebaseSyncHelper.deleteTransaction(transaction)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            database.goalDao().deleteGoal(goal)
            FirebaseSyncHelper.deleteGoal(goal)
        }
    }

    fun deleteRiskProfile(riskProfile: RiskProfileEntity) {
        viewModelScope.launch {
            database.riskProfileDao().deleteRiskProfile(riskProfile)
        }
    }
}
