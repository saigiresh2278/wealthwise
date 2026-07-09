package com.example.wealthwiseai.data.local.dao

import androidx.room.*
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC")
    fun getAllTransactions(email: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsAdmin(): Flow<List<TransactionEntity>>
}
