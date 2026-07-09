package com.example.wealthwiseai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.wealthwiseai.data.local.entity.AuthEntity

@Dao
interface AuthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AuthEntity)

    @Query("SELECT * FROM auth_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): AuthEntity?

    @Query("SELECT * FROM auth_users")
    fun getAllUsers(): kotlinx.coroutines.flow.Flow<List<AuthEntity>>

    @Delete
    suspend fun deleteUser(user: AuthEntity)
}
