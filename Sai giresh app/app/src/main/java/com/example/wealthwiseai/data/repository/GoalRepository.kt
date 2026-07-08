package com.example.wealthwiseai.data.repository

import com.example.wealthwiseai.data.local.dao.GoalDao
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {
    
    fun getGoals(email: String): Flow<List<GoalEntity>> {
        return goalDao.getAllGoals(email)
    }

    suspend fun insertGoal(goal: GoalEntity) {
        val id = goalDao.insertGoal(goal)
        val syncedGoal = goal.copy(id = id)
        FirebaseSyncHelper.syncGoal(syncedGoal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
        FirebaseSyncHelper.syncGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
        FirebaseSyncHelper.deleteGoal(goal)
    }

    suspend fun clearGoals() {
        goalDao.clearGoals()
    }
}
