package com.example.wealthwiseai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long,
    val type: String // "Income" or "Expense"
)
