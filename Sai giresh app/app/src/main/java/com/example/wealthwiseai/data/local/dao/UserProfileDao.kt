package com.example.wealthwiseai.data.local.dao

import androidx.room.*
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE email = :email LIMIT 1")
    fun getUserProfile(email: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmailDirect(email: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()
}
