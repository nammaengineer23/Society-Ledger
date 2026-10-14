package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to record all Super Admin modifications and deletions for full transparency.
 * Accessible to all committee members and residents in Reports.
 */
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionType: String, // "DONATION" or "EXPENSE" or "SOCIETY" or "MEMBER"
    val transactionId: Long = 0L,
    val transactionRef: String = "", // e.g. "RCP-2026-001" or Expense Title
    val action: String, // "MODIFIED", "DELETED", "STATUS_CHANGED", "PIN_RESET"
    val reason: String, // Compulsory reason provided by Super Admin
    val performedById: Long = 1L,
    val performedByName: String = "Super Admin",
    val performedByRole: String = "Super Admin",
    val oldValueSummary: String = "",
    val newValueSummary: String = "",
    val societyId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val firestoreId: String = ""
)

/**
 * Unified model representing any modified or deleted transaction with reasons and audit details.
 */
data class AuditedTransactionItem(
    val id: Long,
    val firestoreId: String = "",
    val type: String, // "DONATION" or "EXPENSE"
    val reference: String, // Receipt number or Expense title
    val entityName: String, // Donor name or Vendor/PaidTo name
    val amount: Double,
    val isDeleted: Boolean,
    val isModified: Boolean,
    val reason: String,
    val timestamp: Long,
    val performedBy: String,
    val details: String = ""
)
