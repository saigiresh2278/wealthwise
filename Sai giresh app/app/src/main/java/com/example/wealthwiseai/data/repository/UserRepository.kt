package com.example.wealthwiseai.data.repository

import com.example.wealthwiseai.data.local.dao.UserProfileDao
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userProfileDao: UserProfileDao) {
    
    fun getUserProfile(email: String): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile(email)
    }

    suspend fun getProfileByEmailDirect(email: String): UserProfileEntity? {
        return userProfileDao.getProfileByEmailDirect(email)
    }

    suspend fun insertUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertUserProfile(profile)
        FirebaseSyncHelper.syncUserProfile(profile)
    }

    suspend fun clearProfile() {
        userProfileDao.clearProfile()
    }
}
