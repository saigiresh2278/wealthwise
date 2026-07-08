package com.example.wealthwiseai.data.repository

import com.example.wealthwiseai.data.local.dao.RiskProfileDao
import com.example.wealthwiseai.data.local.entity.RiskProfileEntity
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import kotlinx.coroutines.flow.Flow

class RiskRepository(private val riskProfileDao: RiskProfileDao) {
    
    fun getRiskProfile(email: String): Flow<RiskProfileEntity?> {
        return riskProfileDao.getRiskProfile(email)
    }

    suspend fun insertRiskProfile(profile: RiskProfileEntity) {
        riskProfileDao.insertRiskProfile(profile)
        FirebaseSyncHelper.syncRiskProfile(profile)
    }

    suspend fun clearRiskProfile() {
        riskProfileDao.clearRiskProfile()
    }
}
