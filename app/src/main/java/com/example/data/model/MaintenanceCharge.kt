package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A maintenance charge assessed against a flat for a billing period.
 *
 * Payments are recorded as offline/external references only. This entity does not
 * initiate, collect, or process payments.
 */
@Entity(tableName = "maintenance_charges", indices = [Index(value = ["flatNo", "billingPeriod"], unique = true)])
data class MaintenanceCharge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val flatNo: String,
    val memberId: Long = 0,
    val memberName: String = "",
    /** Stable period key such as 2026-10. */
    val billingPeriod: String,
    val amount: Double,
    val dueDate: Long,
    /** OPEN, PARTIALLY_PAID, PAID, or WAIVED. */
    val status: String = "OPEN",
    val amountPaid: Double = 0.0,
    /** Cash, UPI, bank transfer, cheque, or another external method. */
    val paymentMode: String = "",
    /** Optional reference entered by the treasurer; never a payment credential. */
    val paymentReference: String = "",
    val notes: String = "",
    val recordedById: Long = 0,
    val recordedByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val outstandingAmount: Double
        get() = (amount - amountPaid).coerceAtLeast(0.0)
}
