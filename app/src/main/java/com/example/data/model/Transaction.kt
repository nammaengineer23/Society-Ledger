package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core Financial Transaction Room Entity & Firestore Data Model.
 * Represents both income/receipts and expenditures/debits for society ledger accounting.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionType: String = "INCOME", // Includes MEMBER_OPENING_BALANCE, MEMBER_EXPENSE, SETTLEMENT_TO_MEMBER, SETTLEMENT_FROM_MEMBER
    val committeeMemberId: Long = 0L, // 0 means not linked to a committee member
    val title: String, // Brief summary or purpose of transaction
    val category: String, // e.g., "Maintenance Fee", "Events & Festivals", "Utilities", "Repairs"
    val amount: Double,
    val partyName: String, // Resident, Donor, Vendor, or Payee
    val partyPhone: String = "",
    val partyEmail: String = "",
    val paymentMode: String = "Cash", // "Cash", "UPI/Online", "Bank Transfer", "Cheque"
    val paymentDate: Long = System.currentTimeMillis(), // Timestamp of transaction
    val status: String = "COMPLETED", // "COMPLETED", "PENDING", "VOIDED", "CANCELLED"
    val receiptNo: String = "", // Assigned receipt / voucher / bill voucher number
    val referenceNo: String = "", // Bank UTR, Cheque No, or payment reference ID
    val notes: String = "",
    val recordedById: Long = 1L,
    val recordedByName: String = "Admin",
    val userId: String = "", // Firebase Auth UID
    val userEmail: String = "", // Logged-in user email
    val societyId: String = "", // Associated society registration or ID
    val firestoreId: String = "", // Document ID in Firebase Firestore
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
     * Converts this Transaction entity into a Firestore-compatible Map for cloud sync.
     */
    fun toFirestoreMap(): HashMap<String, Any?> {
        return hashMapOf(
            "localId" to id,
            "transactionType" to transactionType,
            "committeeMemberId" to committeeMemberId,
            "title" to title,
            "category" to category,
            "amount" to amount,
            "partyName" to partyName,
            "partyPhone" to partyPhone,
            "partyEmail" to partyEmail,
            "paymentMode" to paymentMode,
            "paymentDate" to paymentDate,
            "status" to status,
            "receiptNo" to receiptNo,
            "referenceNo" to referenceNo,
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

    /**
     * Interoperability helper to convert to Donation model.
     */
    fun toDonation(): Donation {
        return Donation(
            id = id,
            donorName = partyName,
            phone = partyPhone,
            amount = amount,
            status = if (status.equals("PENDING", ignoreCase = true)) "pending" else "received",
            paymentMode = paymentMode,
            paymentDate = paymentDate,
            receiptNo = receiptNo.ifBlank { "REC-$id" },
            notes = if (notes.isNotBlank()) notes else title,
            recordedById = recordedById,
            recordedByName = recordedByName,
            userId = userId,
            userEmail = userEmail,
            firestoreId = firestoreId,
            societyId = societyId,
            isModified = isModified,
            modificationReason = modificationReason,
            modifiedAt = modifiedAt,
            modifiedByName = modifiedByName,
            isDeleted = isDeleted,
            deletionReason = deletionReason,
            deletedAt = deletedAt,
            deletedByName = deletedByName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Interoperability helper to convert to Expense model.
     */
    fun toExpense(): Expense {
        return Expense(
            id = id,
            title = title,
            category = category,
            amount = amount,
            expenseDate = paymentDate,
            paidTo = partyName,
            paymentMode = paymentMode,
            receiptUrl = receiptNo,
            recordedById = recordedById,
            recordedByName = recordedByName,
            userId = userId,
            userEmail = userEmail,
            firestoreId = firestoreId,
            societyId = societyId,
            isModified = isModified,
            modificationReason = modificationReason,
            modifiedAt = modifiedAt,
            modifiedByName = modifiedByName,
            isDeleted = isDeleted,
            deletionReason = deletionReason,
            deletedAt = deletedAt,
            deletedByName = deletedByName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Reconstructs a Transaction from Firestore document data.
         */
        fun fromFirestoreDoc(docId: String, data: Map<String, Any?>): Transaction {
            return Transaction(
                id = (data["localId"] as? Number)?.toLong() ?: 0L,
                transactionType = data["transactionType"] as? String ?: "INCOME",
                committeeMemberId = (data["committeeMemberId"] as? Number)?.toLong() ?: 0L,
                title = data["title"] as? String ?: "",
                category = data["category"] as? String ?: "General",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                partyName = data["partyName"] as? String ?: "",
                partyPhone = data["partyPhone"] as? String ?: "",
                partyEmail = data["partyEmail"] as? String ?: "",
                paymentMode = data["paymentMode"] as? String ?: "Cash",
                paymentDate = (data["paymentDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                status = data["status"] as? String ?: "COMPLETED",
                receiptNo = data["receiptNo"] as? String ?: "",
                referenceNo = data["referenceNo"] as? String ?: "",
                notes = data["notes"] as? String ?: "",
                recordedById = (data["recordedById"] as? Number)?.toLong() ?: 1L,
                recordedByName = data["recordedByName"] as? String ?: "Admin",
                userId = data["userId"] as? String ?: "",
                userEmail = data["userEmail"] as? String ?: "",
                societyId = data["societyId"] as? String ?: "",
                firestoreId = docId,
                isModified = data["isModified"] as? Boolean ?: false,
                modificationReason = data["modificationReason"] as? String ?: "",
                modifiedAt = (data["modifiedAt"] as? Number)?.toLong() ?: 0L,
                modifiedByName = data["modifiedByName"] as? String ?: "",
                isDeleted = data["isDeleted"] as? Boolean ?: false,
                deletionReason = data["deletionReason"] as? String ?: "",
                deletedAt = (data["deletedAt"] as? Number)?.toLong() ?: 0L,
                deletedByName = data["deletedByName"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun fromDonation(donation: Donation): Transaction {
            return Transaction(
                id = donation.id,
                transactionType = if (donation.status.equals("pending", ignoreCase = true)) "PLEDGE" else "DONATION",
                title = if (donation.notes.isNotBlank()) donation.notes else "Contribution by ${donation.donorName}",
                category = "Donations & Contributions",
                amount = donation.amount,
                partyName = donation.donorName,
                partyPhone = donation.phone,
                partyEmail = donation.userEmail,
                paymentMode = donation.paymentMode,
                paymentDate = donation.paymentDate,
                status = if (donation.status.equals("pending", ignoreCase = true)) "PENDING" else "COMPLETED",
                receiptNo = donation.receiptNo,
                referenceNo = donation.receiptNo,
                notes = donation.notes,
                recordedById = donation.recordedById,
                recordedByName = donation.recordedByName,
                userId = donation.userId,
                userEmail = donation.userEmail,
                societyId = donation.societyId,
                firestoreId = donation.firestoreId,
                isModified = donation.isModified,
                modificationReason = donation.modificationReason,
                modifiedAt = donation.modifiedAt,
                modifiedByName = donation.modifiedByName,
                isDeleted = donation.isDeleted,
                deletionReason = donation.deletionReason,
                deletedAt = donation.deletedAt,
                deletedByName = donation.deletedByName,
                createdAt = donation.createdAt,
                updatedAt = donation.updatedAt
            )
        }

        fun fromExpense(expense: Expense): Transaction {
            return Transaction(
                id = expense.id,
                transactionType = "EXPENSE",
                title = expense.title,
                category = expense.category,
                amount = expense.amount,
                partyName = expense.paidTo,
                partyPhone = "",
                partyEmail = expense.userEmail,
                paymentMode = expense.paymentMode,
                paymentDate = expense.expenseDate,
                status = "COMPLETED",
                receiptNo = expense.receiptUrl,
                referenceNo = expense.receiptUrl,
                notes = expense.title,
                recordedById = expense.recordedById,
                recordedByName = expense.recordedByName,
                userId = expense.userId,
                userEmail = expense.userEmail,
                societyId = expense.societyId,
                firestoreId = expense.firestoreId,
                isModified = expense.isModified,
                modificationReason = expense.modificationReason,
                modifiedAt = expense.modifiedAt,
                modifiedByName = expense.modifiedByName,
                isDeleted = expense.isDeleted,
                deletionReason = expense.deletionReason,
                deletedAt = expense.deletedAt,
                deletedByName = expense.deletedByName,
                createdAt = expense.createdAt,
                updatedAt = expense.updatedAt
            )
        }
    }
}
