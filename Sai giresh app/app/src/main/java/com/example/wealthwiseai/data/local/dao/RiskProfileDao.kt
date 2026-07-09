package com.example.wealthwiseai.data.local.dao

import androidx.room.*
import com.example.wealthwiseai.data.local.entity.RiskProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskProfileDao {
    @Query("SELECT * FROM risk_profiles WHERE email = :email LIMIT 1")
    fun getRiskProfile(email: String): Flow<RiskProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskProfile(profile: RiskProfileEntity)

    @Query("DELETE FROM risk_profiles")
    suspend fun clearRiskProfile()

    @Query("SELECT * FROM risk_profiles")
    fun getAllRiskProfiles(): Flow<List<RiskProfileEntity>>

    @Delete
    suspend fun deleteRiskProfile(profile: RiskProfileEntity)
}
