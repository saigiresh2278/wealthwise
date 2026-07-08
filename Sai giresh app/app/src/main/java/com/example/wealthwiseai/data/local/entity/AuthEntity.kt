package com.example.wealthwiseai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_users")
data class AuthEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val passwordHash: String
)
