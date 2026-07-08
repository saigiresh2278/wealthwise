package com.example.wealthwiseai.ai

import com.example.wealthwiseai.data.local.entity.GoalEntity

object GoalPlannerEngine {

    data class GoalAnalysis(
        val remainingAmount: Double,
        val monthlySavingsRequired: Double,
        val completionPercentage: Float,
        val monthsRemaining: Long,
        val isAchievable: Boolean
    )

    fun analyzeGoal(goal: GoalEntity, monthlySavingsCapacity: Double): GoalAnalysis {
        val remaining = (goal.targetAmount - goal.currentSavedAmount).coerceAtLeast(0.0)
        
        // Calculate months remaining
        val currentTimeMs = System.currentTimeMillis()
        val timeDifferenceMs = goal.targetDate - currentTimeMs
        val oneMonthMs = 30L * 24 * 60 * 60 * 1000
        val monthsRemaining = (timeDifferenceMs / oneMonthMs).coerceAtLeast(1)

        val monthlyRequired = if (remaining > 0.0) remaining / monthsRemaining else 0.0
        val completionPercentage = if (goal.targetAmount > 0.0) {
            (goal.currentSavedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else {
            1f
        }

        val isAchievable = monthlyRequired <= monthlySavingsCapacity

        return GoalAnalysis(
            remainingAmount = remaining,
            monthlySavingsRequired = monthlyRequired,
            completionPercentage = completionPercentage,
            monthsRemaining = monthsRemaining,
            isAchievable = isAchievable
        )
    }
}
