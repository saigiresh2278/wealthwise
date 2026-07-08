package com.example.wealthwiseai.data.repository

import com.example.wealthwiseai.data.local.dao.TransactionDao
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    fun getTransactions(email: String): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions(email)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        val id = transactionDao.insertTransaction(transaction)
        val syncedTransaction = transaction.copy(id = id)
        FirebaseSyncHelper.syncTransaction(syncedTransaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
        FirebaseSyncHelper.syncTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        FirebaseSyncHelper.deleteTransaction(transaction)
    }

    suspend fun clearTransactions() {
        transactionDao.clearTransactions()
    }
}
