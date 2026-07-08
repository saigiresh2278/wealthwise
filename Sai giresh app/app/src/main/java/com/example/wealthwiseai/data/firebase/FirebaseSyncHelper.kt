package com.example.wealthwiseai.data.firebase

import android.content.Context
import android.util.Log
import com.example.wealthwiseai.data.local.entity.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseSyncHelper {
    private const val TAG = "FirebaseSyncHelper"

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firestore instance: ${e.message}")
            null
        }
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
                val firestore = db ?: return@launch
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
                firestore.collection("users").document(profile.email).set(data)
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
                val firestore = db ?: return@launch
                val docId = "${transaction.userEmail}_${transaction.id}"
                val data = mapOf(
                    "id" to transaction.id,
                    "userEmail" to transaction.userEmail,
                    "amount" to transaction.amount,
                    "category" to transaction.category,
                    "note" to transaction.note,
                    "date" to transaction.date,
                    "type" to transaction.type
                )
                firestore.collection("transactions").document(docId).set(data)
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
                val firestore = db ?: return@launch
                val docId = "${transaction.userEmail}_${transaction.id}"
                firestore.collection("transactions").document(docId).delete()
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
                val firestore = db ?: return@launch
                val docId = "${goal.userEmail}_${goal.id}"
                val data = mapOf(
                    "id" to goal.id,
                    "userEmail" to goal.userEmail,
                    "goalName" to goal.goalName,
                    "targetAmount" to goal.targetAmount,
                    "currentSavedAmount" to goal.currentSavedAmount,
                    "targetDate" to goal.targetDate,
                    "priority" to goal.priority
                )
                firestore.collection("goals").document(docId).set(data)
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
                val firestore = db ?: return@launch
                val docId = "${goal.userEmail}_${goal.id}"
                firestore.collection("goals").document(docId).delete()
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
                val firestore = db ?: return@launch
                val docId = riskProfile.email
                val data = mapOf(
                    "email" to riskProfile.email,
                    "score" to riskProfile.score,
                    "riskClass" to riskProfile.riskClass,
                    "lastAssessmentDate" to riskProfile.lastAssessmentDate
                )
                firestore.collection("risk_profiles").document(docId).set(data)
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
                val firestore = db ?: return@launch
                val data = mapOf(
                    "email" to user.email,
                    "fullName" to user.fullName,
                    "passwordHash" to user.passwordHash
                )
                firestore.collection("auth_users").document(user.email).set(data)
                    .addOnSuccessListener { Log.d(TAG, "AuthUser synced successfully") }
                    .addOnFailureListener { e -> Log.e(TAG, "AuthUser sync failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing AuthUser: ${e.message}")
            }
        }
    }

    suspend fun fetchAuthUserDirect(email: String): AuthEntity? {
        return try {
            val firestore = db ?: return null
            val document = firestore.collection("auth_users").document(email).get().await()
            if (document.exists()) {
                AuthEntity(
                    email = document.getString("email") ?: "",
                    fullName = document.getString("fullName") ?: "",
                    passwordHash = document.getString("passwordHash") ?: ""
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
                val firestore = db ?: return@launch
                // Delete user profile
                firestore.collection("users").document(email).delete()
                
                // Delete auth profile
                firestore.collection("auth_users").document(email).delete()

                // Delete risk profile
                firestore.collection("risk_profiles").document(email).delete()
                
                // Delete user transactions
                firestore.collection("transactions")
                    .whereEqualTo("userEmail", email)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                        Log.d(TAG, "User transactions cleared from sync")
                    }
                
                // Delete user goals
                firestore.collection("goals")
                    .whereEqualTo("userEmail", email)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                        Log.d(TAG, "User goals cleared from sync")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing user data from sync: ${e.message}")
            }
        }
    }
}
