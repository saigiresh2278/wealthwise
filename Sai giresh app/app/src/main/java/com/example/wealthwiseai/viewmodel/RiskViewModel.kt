package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.RiskProfileEntity
import com.example.wealthwiseai.data.repository.RiskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RiskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RiskRepository
    private val sessionManager: SessionManager
    val riskProfile: StateFlow<RiskProfileEntity?>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RiskRepository(database.riskProfileDao())
        sessionManager = SessionManager.getInstance(application)

        riskProfile = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) repository.getRiskProfile(email) else flowOf(null)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun saveRiskProfile(score: Int, riskClass: String) {
        val email = sessionManager.userEmail.value ?: return
        viewModelScope.launch {
            val entity = RiskProfileEntity(
                email = email,
                score = score,
                riskClass = riskClass,
                lastAssessmentDate = System.currentTimeMillis()
            )
            repository.insertRiskProfile(entity)
        }
    }
}
