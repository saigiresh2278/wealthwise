package com.example.wealthwiseai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "risk_profiles")
data class RiskProfileEntity(
    @PrimaryKey val email: String,
    val score: Int,
    val riskClass: String, // "Low Risk", "Medium Risk", "High Risk"
    val lastAssessmentDate: Long
)
