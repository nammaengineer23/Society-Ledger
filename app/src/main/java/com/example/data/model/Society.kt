package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity & Domain model representing a Society in Firestore / local storage.
 */
@Entity(tableName = "societies")
data class Society(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val registrationNo: String = "",
    val upiId: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val branchName: String = "",
    val bankAccount: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pinCode: String = "",
    val contactEmail: String = "",
    val adminEmail: String = "",
    val contactPhone: String = "",
    val financialYear: String = "FY 2026-2027",
    val openingBalance: Double = 0.0,
    val presidentName: String = "",
    val secretaryName: String = "",
    val treasurerName: String = "",
    val isPublic: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toSocietyInfo(): SocietyInfo {
        return SocietyInfo(
            name = name,
            registrationNo = registrationNo,
            upiId = upiId,
            bankName = bankName,
            accountNumber = accountNumber,
            ifscCode = ifscCode,
            branchName = branchName,
            bankAccount = bankAccount,
            address = address,
            city = city,
            state = state,
            pinCode = pinCode,
            contactEmail = contactEmail,
            adminEmail = adminEmail,
            contactPhone = contactPhone,
            financialYear = financialYear,
            openingBalance = openingBalance,
            presidentName = presidentName,
            secretaryName = secretaryName,
            treasurerName = treasurerName
        )
    }
}

typealias SearchableSociety = Society

/**
 * Converter from SocietyInfo to Society.
 */
fun SocietyInfo.toSociety(id: String? = null): Society {
    val docId = id ?: (if (registrationNo.isNotBlank()) registrationNo.replace("/", "_").replace(" ", "_") else "default_society")
    return Society(
        id = docId,
        name = name,
        registrationNo = registrationNo,
        upiId = upiId,
        bankName = bankName,
        accountNumber = accountNumber,
        ifscCode = ifscCode,
        branchName = branchName,
        bankAccount = bankAccount,
        address = address,
        city = city,
        state = state,
        pinCode = pinCode,
        contactEmail = contactEmail,
        adminEmail = adminEmail,
        contactPhone = contactPhone,
        financialYear = financialYear,
        openingBalance = openingBalance,
        presidentName = presidentName,
        secretaryName = secretaryName,
        treasurerName = treasurerName,
        isPublic = true,
        updatedAt = System.currentTimeMillis()
    )
}
