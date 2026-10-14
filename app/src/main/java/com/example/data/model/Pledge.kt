package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core Pledges Room Entity & Firestore Data Model.
 * Specifically tracks committed contributions, upcoming dues, and pledge fulfillment.
 */
@Entity(tableName = "pledges")
data class Pledge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val donorName: String, // Resident or donor who made the commitment
    val phone: String, // Contact phone number
    val email: String = "",
    val flatNo: String = "", // Residential flat or unit number
    val amount: Double, // Total pledged contribution amount (₹)
    val fulfilledAmount: Double = 0.0, // Partial or full amount collected
    val pledgeDate: Long = System.currentTimeMillis(), // Date commitment was entered
    val targetDate: Long = 0L, // Promised fulfillment or deadline date
    val status: String = "PENDING", // "PENDING", "PARTIALLY_PAID", "FULFILLED", "CANCELLED"
    val purpose: String = "Society Contribution", // e.g. "Festival Utsav", "Elevator Modernization", "General Fund"
    val paymentModePreference: String = "UPI/Online",
    val receiptNo: String = "", // Generated upon collection/fulfillment
    val notes: String = "",
    val reminderSentCount: Int = 0, // WhatsApp or SMS reminder counter
    val lastReminderSentAt: Long? = null,
    val fulfillmentDate: Long? = null,
    val recordedById: Long = 1L,
    val recordedByName: String = "Admin",
    val userId: String = "", // Firebase UID
    val userEmail: String = "",
    val societyId: String = "",
    val firestoreId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculated remaining pledge balance.
     */
    val remainingAmount: Double
        get() = (amount - fulfilledAmount).coerceAtLeast(0.0)

    /**
     * Checks if the pledged commitment is fully completed.
     */
    val isFullyPaid: Boolean
        get() = fulfilledAmount >= amount && amount > 0.0

    /**
     * Converts Pledge to Firestore-compatible Map for cloud synchronization.
     */
    fun toFirestoreMap(): HashMap<String, Any?> {
        return hashMapOf(
            "localId" to id,
            "donorName" to donorName,
            "phone" to phone,
            "email" to email,
            "flatNo" to flatNo,
            "amount" to amount,
            "fulfilledAmount" to fulfilledAmount,
            "pledgeDate" to pledgeDate,
            "targetDate" to targetDate,
            "status" to status,
            "purpose" to purpose,
            "paymentModePreference" to paymentModePreference,
            "receiptNo" to receiptNo,
            "notes" to notes,
            "reminderSentCount" to reminderSentCount,
            "lastReminderSentAt" to lastReminderSentAt,
            "fulfillmentDate" to fulfillmentDate,
            "recordedById" to recordedById,
            "recordedByName" to recordedByName,
            "userId" to userId,
            "userEmail" to userEmail,
            "societyId" to societyId,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    /**
     * Interoperability helper to convert to Donation model (status = "pending" or "received").
     */
    fun toDonation(): Donation {
        return Donation(
            id = id,
            donorName = donorName,
            phone = phone,
            amount = amount,
            status = if (status.equals("FULFILLED", ignoreCase = true)) "received" else "pending",
            paymentMode = paymentModePreference,
            paymentDate = pledgeDate,
            receiptNo = receiptNo.ifBlank { "PLG-$id" },
            notes = if (notes.isNotBlank()) notes else purpose,
            recordedById = recordedById,
            recordedByName = recordedByName,
            userId = userId,
            userEmail = userEmail,
            firestoreId = firestoreId,
            societyId = societyId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Reconstructs a Pledge from Firestore document data.
         */
        fun fromFirestoreDoc(docId: String, data: Map<String, Any?>): Pledge {
            return Pledge(
                id = (data["localId"] as? Number)?.toLong() ?: 0L,
                donorName = data["donorName"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                email = data["email"] as? String ?: "",
                flatNo = data["flatNo"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                fulfilledAmount = (data["fulfilledAmount"] as? Number)?.toDouble() ?: 0.0,
                pledgeDate = (data["pledgeDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                targetDate = (data["targetDate"] as? Number)?.toLong() ?: 0L,
                status = data["status"] as? String ?: "PENDING",
                purpose = data["purpose"] as? String ?: "Society Contribution",
                paymentModePreference = data["paymentModePreference"] as? String ?: "UPI/Online",
                receiptNo = data["receiptNo"] as? String ?: "",
                notes = data["notes"] as? String ?: "",
                reminderSentCount = (data["reminderSentCount"] as? Number)?.toInt() ?: 0,
                lastReminderSentAt = (data["lastReminderSentAt"] as? Number)?.toLong(),
                fulfillmentDate = (data["fulfillmentDate"] as? Number)?.toLong(),
                recordedById = (data["recordedById"] as? Number)?.toLong() ?: 1L,
                recordedByName = data["recordedByName"] as? String ?: "Admin",
                userId = data["userId"] as? String ?: "",
                userEmail = data["userEmail"] as? String ?: "",
                societyId = data["societyId"] as? String ?: "",
                firestoreId = docId,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun fromDonation(donation: Donation): Pledge {
            val isReceived = donation.status.equals("received", ignoreCase = true)
            return Pledge(
                id = donation.id,
                donorName = donation.donorName,
                phone = donation.phone,
                email = donation.userEmail,
                amount = donation.amount,
                fulfilledAmount = if (isReceived) donation.amount else 0.0,
                pledgeDate = donation.paymentDate,
                status = if (isReceived) "FULFILLED" else "PENDING",
                purpose = donation.notes.ifBlank { "Society Contribution" },
                paymentModePreference = donation.paymentMode,
                receiptNo = donation.receiptNo,
                notes = donation.notes,
                recordedById = donation.recordedById,
                recordedByName = donation.recordedByName,
                userId = donation.userId,
                userEmail = donation.userEmail,
                societyId = donation.societyId,
                firestoreId = donation.firestoreId,
                createdAt = donation.createdAt,
                updatedAt = donation.updatedAt
            )
        }
    }
}
