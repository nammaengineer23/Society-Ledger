package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Utilities", "Events & Festivals", "Maintenance", "Refreshments", "Repairs", "Miscellaneous", "Admin & Legal"
    val amount: Double,
    val expenseDate: Long, // timestamp
    val paidTo: String, // Vendor or Person name
    val paymentMode: String, // "Cash", "Online", "Cheque", "UPI", "Bank Transfer"
    val receiptUrl: String = "", // Receipt note / reference / bill no
    val recordedById: Long = 1,
    val recordedByName: String = "Admin",
    val userId: String = "", // Associated Firebase UID or Member ID
    val userEmail: String = "", // Associated Logged-in User Email
    val firestoreId: String = "", // Firestore Document ID for cloud sync
    val societyId: String = "",
    val isModified: Boolean = false,
    val modificationReason: String = "",
    val modifiedAt: Long = 0L,
    val modifiedByName: String = "",
    val isDeleted: Boolean = false,
    val deletionReason: String = "",
    val deletedAt: Long = 0L,
    val deletedByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts this Expense object into a Firestore-compatible Map for cloud storage.
     */
    fun toFirestoreMap(): HashMap<String, Any?> {
        return hashMapOf(
            "localId" to id,
            "title" to title,
            "category" to category,
            "amount" to amount,
            "expenseDate" to expenseDate,
            "paidTo" to paidTo,
            "paymentMode" to paymentMode,
            "receiptUrl" to receiptUrl,
            "recordedById" to recordedById,
            "recordedByName" to recordedByName,
            "userId" to userId,
            "userEmail" to userEmail,
            "societyId" to societyId,
            "isModified" to isModified,
            "modificationReason" to modificationReason,
            "modifiedAt" to modifiedAt,
            "modifiedByName" to modifiedByName,
            "isDeleted" to isDeleted,
            "deletionReason" to deletionReason,
            "deletedAt" to deletedAt,
            "deletedByName" to deletedByName,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    companion object {
        /**
         * Reconstructs an Expense entity from a Firestore DocumentSnapshot safely.
         */
        fun fromFirestore(doc: DocumentSnapshot): Expense? {
            return try {
                Expense(
                    id = doc.getLong("localId") ?: 0L,
                    title = doc.getString("title") ?: "",
                    category = doc.getString("category") ?: "Miscellaneous",
                    amount = doc.getDouble("amount") ?: (doc.getLong("amount")?.toDouble() ?: 0.0),
                    expenseDate = doc.getLong("expenseDate") ?: System.currentTimeMillis(),
                    paidTo = doc.getString("paidTo") ?: "",
                    paymentMode = doc.getString("paymentMode") ?: "Cash",
                    receiptUrl = doc.getString("receiptUrl") ?: "",
                    recordedById = doc.getLong("recordedById") ?: 1L,
                    recordedByName = doc.getString("recordedByName") ?: "Admin",
                    userId = doc.getString("userId") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    firestoreId = doc.id,
                    societyId = doc.getString("societyId") ?: "",
                    isModified = doc.getBoolean("isModified") ?: false,
                    modificationReason = doc.getString("modificationReason") ?: "",
                    modifiedAt = doc.getLong("modifiedAt") ?: 0L,
                    modifiedByName = doc.getString("modifiedByName") ?: "",
                    isDeleted = doc.getBoolean("isDeleted") ?: false,
                    deletionReason = doc.getString("deletionReason") ?: "",
                    deletedAt = doc.getLong("deletedAt") ?: 0L,
                    deletedByName = doc.getString("deletedByName") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
