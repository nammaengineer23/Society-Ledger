package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val donorName: String,
    val phone: String,
    val amount: Double,
    val status: String, // "received" or "pending"
    val paymentMode: String, // "Cash", "UPI/Online", "Bank Transfer", "Cheque"
    val paymentDate: Long, // timestamp
    val receiptNo: String, // e.g., "RCP-2026-001"
    val notes: String = "",
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
     * Converts this Donation object into a Firestore-compatible Map for cloud storage.
     */
    fun toFirestoreMap(): HashMap<String, Any?> {
        return hashMapOf(
            "localId" to id,
            "donorName" to donorName,
            "phone" to phone,
            "amount" to amount,
            "status" to status,
            "paymentMode" to paymentMode,
            "paymentDate" to paymentDate,
            "receiptNo" to receiptNo,
            "notes" to notes,
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
         * Reconstructs a Donation entity from a Firestore DocumentSnapshot safely.
         */
        fun fromFirestore(doc: DocumentSnapshot): Donation? {
            return try {
                Donation(
                    id = doc.getLong("localId") ?: 0L,
                    donorName = doc.getString("donorName") ?: "",
                    phone = doc.getString("phone") ?: "",
                    amount = doc.getDouble("amount") ?: (doc.getLong("amount")?.toDouble() ?: 0.0),
                    status = doc.getString("status") ?: "received",
                    paymentMode = doc.getString("paymentMode") ?: "Cash",
                    paymentDate = doc.getLong("paymentDate") ?: System.currentTimeMillis(),
                    receiptNo = doc.getString("receiptNo") ?: "",
                    notes = doc.getString("notes") ?: "",
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

