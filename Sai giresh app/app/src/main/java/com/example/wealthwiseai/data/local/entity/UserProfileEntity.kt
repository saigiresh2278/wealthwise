package com.example.wealthwiseai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val age: Int,
    val occupation: String,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val monthlySavings: Double,
    val mainFinancialGoal: String,
    val riskComfort: String, // Low, Medium, High
    val investmentExperience: String // Beginner, Intermediate, Advanced
)
