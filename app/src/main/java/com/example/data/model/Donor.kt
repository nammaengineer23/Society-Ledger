package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class Donor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val flatOrAddress: String = "",
    val societyId: String = "",
    val notes: String = "",
    val isCustomAdded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class DonorSummary(
    val name: String,
    val phone: String,
    val email: String = "",
    val flatOrAddress: String = "",
    val totalDonated: Double = 0.0,
    val totalReceived: Double = 0.0,
    val totalPending: Double = 0.0,
    val donationCount: Int = 0,
    val lastDonationDate: Long = 0L,
    val paymentModes: List<String> = emptyList(),
    val notes: String = "",
    val societyId: String = ""
)
