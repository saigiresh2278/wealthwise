package com.example.wealthwiseai.data.repository

import com.example.wealthwiseai.data.local.dao.AuthDao
import com.example.wealthwiseai.data.local.entity.AuthEntity
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import java.security.MessageDigest

class AuthRepository(private val authDao: AuthDao) {

    suspend fun registerUser(fullName: String, email: String, password: String): Result<Unit> {
        return try {
            val exists = authDao.userExists(email)
            if (exists) {
                Result.failure(Exception("Email already registered"))
            } else {
                val passwordHash = hashPassword(password)
                val user = AuthEntity(
                    email = email.trim().lowercase(),
                    fullName = fullName.trim(),
                    passwordHash = passwordHash
                )
                authDao.insertUser(user)
                FirebaseSyncHelper.syncAuthUser(user)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authenticateUser(email: String, password: String): Result<AuthEntity> {
        return try {
            var user = authDao.getUserByEmail(email.trim().lowercase())
            if (user == null) {
                // Try fetching synced account from cloud Firestore database
                val cloudUser = FirebaseSyncHelper.fetchAuthUserDirect(email.trim().lowercase())
                if (cloudUser != null) {
                    authDao.insertUser(cloudUser)
                    user = cloudUser
                }
            }
            if (user == null) {
                Result.failure(Exception("Account does not exist"))
            } else {
                val inputHash = hashPassword(password)
                if (user.passwordHash == inputHash) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid password"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): AuthEntity? {
        return authDao.getUserByEmail(email.trim().lowercase())
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
