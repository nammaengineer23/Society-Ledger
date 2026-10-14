package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core Committee Members Room Entity & Firestore Data Model.
 * Represents executive council members, trustees, office bearers, and authorized personnel.
 */
@Entity(tableName = "committee_members")
data class CommitteeMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String, // "President", "Secretary", "Treasurer", "Vice President", "Joint Secretary", "Super Admin", "Executive Member", "Auditor"
    val email: String = "",
    val phone: String = "",
    val flatNo: String = "A-101",
    val pin: String = "1234", // 4-digit committee security PIN for authorization
    val isSuperAdmin: Boolean = false, // Authority to alter society configuration & members
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE", "TERM_EXPIRED", "RESIGNED"
    val termStart: Long = System.currentTimeMillis(),
    val termEnd: Long = 0L, // 0L means indefinite or ongoing
    val responsibilities: String = "", // e.g. "Vendor negotiations, Cheque counter-signing, Audit ledger"
    val emergencyContact: String = "",
    val userId: String = "", // Associated Firebase Auth UID
    val societyId: String = "",
    val firestoreId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts CommitteeMember to Firestore-compatible Map for cloud synchronization.
     */
    fun toFirestoreMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "role" to role,
            "email" to email,
            "phone" to phone,
            "flatNo" to flatNo,
            "pin" to pin,
            "isSuperAdmin" to isSuperAdmin,
            "status" to status,
            "termStart" to termStart,
            "termEnd" to termEnd,
            "responsibilities" to responsibilities,
            "emergencyContact" to emergencyContact,
            "userId" to userId,
            "societyId" to societyId,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    /**
     * Interoperability helper to convert to existing Member model.
     */
    fun toMember(): Member {
        return Member(
            id = id,
            name = name,
            email = email,
            phone = phone,
            role = role,
            pin = pin,
            isSuperAdmin = isSuperAdmin,
            flatNo = flatNo,
            createdAt = createdAt
        )
    }

    companion object {
        /**
         * Reconstructs a CommitteeMember from Firestore document data.
         */
        fun fromFirestoreDoc(docId: String, data: Map<String, Any?>): CommitteeMember {
            return CommitteeMember(
                id = (data["id"] as? Number)?.toLong() ?: 0L,
                name = data["name"] as? String ?: "",
                role = data["role"] as? String ?: "Executive Member",
                email = data["email"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                flatNo = data["flatNo"] as? String ?: "A-101",
                pin = data["pin"] as? String ?: "1234",
                isSuperAdmin = data["isSuperAdmin"] as? Boolean ?: false,
                status = data["status"] as? String ?: "ACTIVE",
                termStart = (data["termStart"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                termEnd = (data["termEnd"] as? Number)?.toLong() ?: 0L,
                responsibilities = data["responsibilities"] as? String ?: "",
                emergencyContact = data["emergencyContact"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                societyId = data["societyId"] as? String ?: "",
                firestoreId = docId,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun fromMember(member: Member): CommitteeMember {
            return CommitteeMember(
                id = member.id,
                name = member.name,
                role = member.role,
                email = member.email,
                phone = member.phone,
                flatNo = member.flatNo,
                pin = member.pin,
                isSuperAdmin = member.isSuperAdmin,
                status = "ACTIVE",
                termStart = member.createdAt,
                termEnd = 0L,
                responsibilities = "Committee operations",
                createdAt = member.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
