package com.example.wealthwiseai.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.repository.UserRepository
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import com.example.wealthwiseai.ui.theme.ThemeStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userRepository: UserRepository = UserRepository(database.userProfileDao())
    private val sessionManager: SessionManager = SessionManager.getInstance(application)
    private val transactionRepository = com.example.wealthwiseai.data.repository.TransactionRepository(database.transactionDao())
    val userProfile: StateFlow<UserProfileEntity?>
    val transactions: StateFlow<List<com.example.wealthwiseai.data.local.entity.TransactionEntity>>

    private val sharedPrefs = application.getSharedPreferences("wealthwise_prefs", Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _themeStyle = MutableStateFlow(
        ThemeStyle.valueOf(sharedPrefs.getString("theme_style", ThemeStyle.CLASSIC_BLUE.name) ?: ThemeStyle.CLASSIC_BLUE.name)
    )
    val themeStyle: StateFlow<ThemeStyle> = _themeStyle

    private val _profilePhotoUri = MutableStateFlow(sharedPrefs.getString("profile_photo_uri", null))
    val profilePhotoUri: StateFlow<String?> = _profilePhotoUri

    init {
        userProfile = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) userRepository.getUserProfile(email) else flowOf(null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        transactions = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) transactionRepository.getTransactions(email) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun saveProfilePhoto(uri: String?) {
        sharedPrefs.edit().putString("profile_photo_uri", uri).apply()
        _profilePhotoUri.value = uri
    }

    fun updateProfile(
        name: String,
        age: Int,
        occupation: String,
        income: Double,
        expenses: Double,
        savings: Double,
        goal: String,
        risk: String,
        experience: String
    ) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val updated = UserProfileEntity(
                email = email,
                fullName = name,
                age = age,
                occupation = occupation,
                monthlyIncome = income,
                monthlyExpenses = expenses,
                monthlySavings = savings,
                mainFinancialGoal = goal,
                riskComfort = risk,
                investmentExperience = experience
            )
            userRepository.insertUserProfile(updated)
        }
    }

    fun logout() {
        sessionManager.logout()
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setThemeStyle(style: ThemeStyle) {
        sharedPrefs.edit().putString("theme_style", style.name).apply()
        _themeStyle.value = style
    }

    fun resetAllData() {
        val email = sessionManager.userEmail.value
        viewModelScope.launch {
            database.userProfileDao().clearProfile()
            database.transactionDao().clearTransactions()
            database.goalDao().clearGoals()
            database.riskProfileDao().clearRiskProfile()
            if (email != null) {
                FirebaseSyncHelper.clearUserData(email)
            }
        }
    }
}
