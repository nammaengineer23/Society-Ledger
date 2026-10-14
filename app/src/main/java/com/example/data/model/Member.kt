package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val role: String, // "President", "Secretary", "Treasurer", "Vice President", "Joint Secretary", "Super Admin", etc.
    val pin: String = "1234", // 4-digit committee security PIN
    val isSuperAdmin: Boolean = false, // Designates Super Admin who can modify members
    val flatNo: String = "A-101",
    val createdAt: Long = System.currentTimeMillis()
)

