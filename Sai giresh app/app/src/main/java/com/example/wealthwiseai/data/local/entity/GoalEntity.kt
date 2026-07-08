package com.example.wealthwiseai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val goalName: String,
    val targetAmount: Double,
    val currentSavedAmount: Double,
    val targetDate: Long,
    val priority: String // "Low", "Medium", "High"
)
