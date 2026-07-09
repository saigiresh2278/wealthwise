package com.example.wealthwiseai.data.firebase

import android.content.Context
import android.util.Log
import com.example.wealthwiseai.data.local.entity.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseSyncHelper {
    private const val TAG = "FirebaseSyncHelper"

    private val db: FirebaseDatabase? by lazy {
        try {
            FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Realtime Database instance: ${e.message}")
            null
        }
    }

    private fun encodeEmail(email: String): String {
        return email.trim().lowercase().replace(".", ",")
    }

    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Firebase initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}")
        }
    }

    fun syncUserProfile(profile: UserProfileEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(profile.email)
                val ref = database.getReference("users").child(emailKey)
                val data = mapOf(
                    "email" to profile.email,
                    "fullName" to profile.fullName,
                    "age" to profile.age,
                    "occupation" to profile.occupation,
                    "monthlyIncome" to profile.monthlyIncome,
                    "monthlyExpenses" to profile.monthlyExpenses,
                    "monthlySavings" to profile.monthlySavings,
                    "mainFinancialGoal" to profile.mainFinancialGoal,
                    "riskComfort" to profile.riskComfort,
                    "investmentExperience" to profile.investmentExperience
                )
                ref.setValue(data)
                    .addOnSuccessListener { Log.d(TAG, "UserProfile synced successfully") }
                    .addOnFailureListener { e -> Log.e(TAG, "UserProfile sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing UserProfile: ${e.message}")
            }
        }
    }

    fun syncTransaction(transaction: TransactionEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(transaction.userEmail)
                val ref = database.getReference("transactions").child(emailKey).child(transaction.id.toString())
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(transaction.date))
                val data = mapOf(
                    "id" to transaction.id,
                    "userEmail" to transaction.userEmail,
                    "amount" to transaction.amount,
                    "category" to transaction.category,
                    "note" to transaction.note,
                    "date" to formattedDate,
                    "type" to transaction.type
                )
                ref.setValue(data)
                    .addOnSuccessListener { Log.d(TAG, "Transaction synced successfully") }
                    .addOnFailureListener { e -> Log.e(TAG, "Transaction sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing Transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(transaction.userEmail)
                val ref = database.getReference("transactions").child(emailKey).child(transaction.id.toString())
                ref.removeValue()
                    .addOnSuccessListener { Log.d(TAG, "Transaction deleted successfully from sync") }
                    .addOnFailureListener { e -> Log.e(TAG, "Transaction deletion from sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting Transaction from sync: ${e.message}")
            }
        }
    }

    fun syncGoal(goal: GoalEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(goal.userEmail)
                val ref = database.getReference("goals").child(emailKey).child(goal.id.toString())
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(goal.targetDate))
                val data = mapOf(
                    "id" to goal.id,
                    "userEmail" to goal.userEmail,
                    "goalName" to goal.goalName,
                    "targetAmount" to goal.targetAmount,
                    "currentSavedAmount" to goal.currentSavedAmount,
                    "targetDate" to formattedDate,
                    "priority" to goal.priority
                )
                ref.setValue(data)
                    .addOnSuccessListener { Log.d(TAG, "Goal synced successfully") }
                    .addOnFailureListener { e -> Log.e(TAG, "Goal sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing Goal: ${e.message}")
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(goal.userEmail)
                val ref = database.getReference("goals").child(emailKey).child(goal.id.toString())
                ref.removeValue()
                    .addOnSuccessListener { Log.d(TAG, "Goal deleted successfully from sync") }
                    .addOnFailureListener { e -> Log.e(TAG, "Goal deletion from sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting Goal from sync: ${e.message}")
            }
        }
    }

    fun syncRiskProfile(riskProfile: RiskProfileEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(riskProfile.email)
                val ref = database.getReference("risk_profiles").child(emailKey)
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(riskProfile.lastAssessmentDate))
                val data = mapOf(
                    "email" to riskProfile.email,
                    "score" to riskProfile.score,
                    "riskClass" to riskProfile.riskClass,
                    "lastAssessmentDate" to formattedDate
                )
                ref.setValue(data)
                    .addOnSuccessListener { Log.d(TAG, "RiskProfile synced successfully") }
                    .addOnFailureListener { e -> Log.e(TAG, "RiskProfile sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing RiskProfile: ${e.message}")
            }
        }
    }

    fun syncAuthUser(user: AuthEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(user.email)
                val ref = database.getReference("auth_users").child(emailKey)
                val data = mapOf(
                    "email" to user.email,
                    "fullName" to user.fullName,
                    "passwordHash" to user.passwordHash
                )
                ref.setValue(data)
                    .addOnSuccessListener { Log.d(TAG, "AuthUser synced successfully") }
                    .addOnFailureListener { e -> Log.e(TAG, "AuthUser sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing AuthUser: ${e.message}")
            }
        }
    }

    suspend fun fetchAuthUserDirect(email: String): AuthEntity? {
        val database = db ?: return null
        val emailKey = encodeEmail(email)
        val ref = database.getReference("auth_users").child(emailKey)
        return try {
            val snapshot = ref.get().await()
            if (snapshot.exists()) {
                AuthEntity(
                    email = snapshot.child("email").getValue(String::class.java) ?: "",
                    fullName = snapshot.child("fullName").getValue(String::class.java) ?: "",
                    passwordHash = snapshot.child("passwordHash").getValue(String::class.java) ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching auth user direct: ${e.message}")
            null
        }
    }

    fun clearUserData(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = db ?: return@launch
                val emailKey = encodeEmail(email)
                database.getReference("users").child(emailKey).removeValue()
                database.getReference("auth_users").child(emailKey).removeValue()
                database.getReference("risk_profiles").child(emailKey).removeValue()
                database.getReference("transactions").child(emailKey).removeValue()
                database.getReference("goals").child(emailKey).removeValue()
                Log.d(TAG, "User data cleared from Realtime Database sync")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing user data from sync: ${e.message}")
            }
        }
    }
}
