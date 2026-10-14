package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model representing a User in the system (Committee Member, Donor, or Public Resident).
 * Fully compatible with Firestore documents and Room local caching.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Member", // "Super Admin", "President", "Secretary", "Treasurer", "Committee Member", "Donor", "Public Resident"
    val pin: String = "1234", // Security PIN for committee actions
    val isSuperAdmin: Boolean = false,
    val userType: String = USER_TYPE_COMMITTEE, // "COMMITTEE", "DONOR", "PUBLIC"
    val societyId: String = "", // Associated Society Registration Number or Firestore ID
    val societyName: String = "",
    val flatOrUnitNo: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val USER_TYPE_COMMITTEE = "COMMITTEE"
        const val USER_TYPE_DONOR = "DONOR"
        const val USER_TYPE_PUBLIC = "PUBLIC"

        const val ROLE_ADMIN = "Admin"
        const val ROLE_SUPER_ADMIN = "Admin"
        const val ROLE_PRESIDENT = "President"
        const val ROLE_SECRETARY = "Secretary"
        const val ROLE_TREASURER = "Treasurer"
        const val ROLE_MEMBER = "Committee Member"
        const val ROLE_DONOR = "Donor"
    }

    /**
     * Converts Member domain model to User representation.
     */
    fun toMember(): Member {
        return Member(
            id = id.toLongOrNull() ?: 0L,
            name = name,
            email = email,
            phone = phone,
            role = role,
            pin = pin,
            isSuperAdmin = isSuperAdmin,
            createdAt = createdAt
        )
    }
}

/**
 * Extension to convert Member to User.
 */
fun Member.toUser(societyId: String = ""): User {
    return User(
        id = if (id > 0) id.toString() else "member_${System.currentTimeMillis()}",
        name = name,
        email = email,
        phone = phone,
        role = role,
        pin = pin,
        isSuperAdmin = isSuperAdmin,
        userType = if (isSuperAdmin || role != "Member") User.USER_TYPE_COMMITTEE else User.USER_TYPE_PUBLIC,
        societyId = societyId,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
