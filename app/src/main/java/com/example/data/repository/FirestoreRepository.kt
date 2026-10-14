package com.example.data.repository

import android.util.Log
import com.example.data.model.Donation
import com.example.data.model.Society
import com.example.data.model.SocietyInfo
import com.example.data.model.User
import com.example.data.model.toSociety
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository interface defining complete CRUD operations for
 * 'Society', 'Donation', and 'User' collections in Firestore.
 */
interface FirestoreRepositoryContract {
    // Society CRUD
    suspend fun createOrUpdateSociety(society: Society): Result<String>
    suspend fun getSociety(societyId: String): Result<Society?>
    fun getSocietyFlow(societyId: String): Flow<Society?>
    suspend fun getAllSocieties(): Result<List<Society>>
    fun getAllSocietiesFlow(): Flow<List<Society>>
    suspend fun updateSociety(societyId: String, updates: Map<String, Any?>): Result<Unit>
    suspend fun deleteSociety(societyId: String): Result<Unit>

    // Donation CRUD
    suspend fun createDonation(donation: Donation): Result<String>
    suspend fun getDonation(donationId: String): Result<Donation?>
    suspend fun getDonationsForSociety(societyId: String = ""): Result<List<Donation>>
    fun getDonationsFlow(societyId: String = ""): Flow<List<Donation>>
    suspend fun getDonationsByDonorPhone(phone: String): Result<List<Donation>>
    suspend fun updateDonation(donationId: String, donation: Donation): Result<Unit>
    suspend fun updateDonationStatus(donationId: String, newStatus: String): Result<Unit>
    suspend fun deleteDonation(donationId: String): Result<Unit>

    // User CRUD
    suspend fun createUser(user: User): Result<String>
    suspend fun getUser(userId: String): Result<User?>
    fun getUserFlow(userId: String): Flow<User?>
    suspend fun getUsersForSociety(societyId: String = ""): Result<List<User>>
    fun getUsersFlow(societyId: String = ""): Flow<List<User>>
    suspend fun getUserByPhone(phone: String): Result<User?>
    suspend fun updateUser(userId: String, user: User): Result<Unit>
    suspend fun updateUserPin(userId: String, newPin: String): Result<Unit>
    suspend fun resetUserPinToDefault(userId: String, defaultPin: String = "1234"): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>

    // Audit Log CRUD
    suspend fun createAuditLog(auditLog: com.example.data.model.AuditLog): Result<String>
    suspend fun getAllAuditLogs(): Result<List<com.example.data.model.AuditLog>>
    fun getAllAuditLogsFlow(): Flow<List<com.example.data.model.AuditLog>>
}

/**
 * Production-ready Firestore Repository for Society, Donation, and User collections.
 * Handles bidirectional model mapping, real-time snapshot listeners with Kotlin Flow,
 * and robust error logging and safety fallbacks.
 */
class FirestoreRepository(
    private val firestoreInstance: FirebaseFirestore? = null,
    private val authInstance: FirebaseAuth? = null
) : FirestoreRepositoryContract {

    private fun logNotice(action: String, e: Throwable) {
        if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            Log.w(TAG, "Notice: $action - Cloud Firestore permissions restricted. Operating in local mode.")
        } else {
            Log.w(TAG, "Notice: $action - ${e.message ?: "network/storage unavailable"}")
        }
    }

    private val db: FirebaseFirestore? by lazy {
        firestoreInstance ?: try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Failed to initialize FirebaseFirestore instance: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        authInstance ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Failed to initialize FirebaseAuth instance: ${e.message}")
            null
        }
    }

    // =========================================================================
    // 1. SOCIETY COLLECTION CRUD OPERATIONS
    // =========================================================================

    /**
     * Create or update a Society document in Firestore.
     * Uses the registration number (or provided ID) as the document ID.
     */
    override suspend fun createOrUpdateSociety(society: Society): Result<String> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val docId = if (society.id.isNotBlank()) {
                society.id
            } else if (society.registrationNo.isNotBlank()) {
                society.registrationNo.replace("/", "_").replace(" ", "_")
            } else {
                firestore.collection(COLLECTION_SOCIETIES).document().id
            }

            val societyData = hashMapOf<String, Any?>(
                "id" to docId,
                "name" to society.name,
                "registrationNo" to society.registrationNo,
                "upiId" to society.upiId,
                "bankName" to society.bankName,
                "accountNumber" to society.accountNumber,
                "ifscCode" to society.ifscCode,
                "branchName" to society.branchName,
                "bankAccount" to society.bankAccount,
                "address" to society.address,
                "contactEmail" to society.contactEmail,
                "contactPhone" to society.contactPhone,
                "financialYear" to society.financialYear,
                "openingBalance" to society.openingBalance,
                "presidentName" to society.presidentName,
                "secretaryName" to society.secretaryName,
                "treasurerName" to society.treasurerName,
                "isPublic" to society.isPublic,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_SOCIETIES)
                .document(docId)
                .set(societyData, SetOptions.merge())
                .await()

            Log.d(TAG, "Society created/updated successfully with doc ID: $docId")
            Result.success(docId)
        } catch (e: Exception) {
            logNotice("Saving Society", e)
            Result.failure(e)
        }
    }

    /**
     * Convenience method to save SocietyInfo.
     */
    suspend fun saveSocietyInfo(societyInfo: SocietyInfo): Result<String> {
        return createOrUpdateSociety(societyInfo.toSociety())
    }

    /**
     * Retrieve a single Society by Document ID.
     */
    override suspend fun getSociety(societyId: String): Result<Society?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val doc = firestore.collection(COLLECTION_SOCIETIES).document(societyId).get().await()
            if (doc.exists()) {
                Result.success(mapDocumentToSociety(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logNotice("Fetching Society $societyId", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Kotlin Flow for single Society document updates.
     */
    override fun getSocietyFlow(societyId: String): Flow<Society?> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(COLLECTION_SOCIETIES)
            .document(societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logNotice("Listening to Society $societyId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(mapDocumentToSociety(snapshot))
                } else {
                    trySend(null)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieve all registered Societies.
     */
    override suspend fun getAllSocieties(): Result<List<Society>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val querySnapshot = firestore.collection(COLLECTION_SOCIETIES).get().await()
            val list = querySnapshot.documents.mapNotNull { mapDocumentToSociety(it) }
            Result.success(list)
        } catch (e: Exception) {
            logNotice("Fetching all Societies", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow for all Societies.
     */
    override fun getAllSocietiesFlow(): Flow<List<Society>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_SOCIETIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logNotice("Listening to Societies collection", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { mapDocumentToSociety(it) } ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Partial update of Society document fields.
     */
    override suspend fun updateSociety(societyId: String, updates: Map<String, Any?>): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val finalUpdates = updates.toMutableMap()
            finalUpdates["updatedAt"] = System.currentTimeMillis()
            firestore.collection(COLLECTION_SOCIETIES).document(societyId).update(finalUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating Society $societyId", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a Society document.
     */
    override suspend fun deleteSociety(societyId: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_SOCIETIES).document(societyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Deleting Society $societyId", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 2. DONATION COLLECTION CRUD OPERATIONS
    // =========================================================================

    /**
     * Create or update a Donation in Firestore.
     */
    override suspend fun createDonation(donation: Donation): Result<String> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val docRef = if (donation.firestoreId.isNotBlank()) {
                firestore.collection(COLLECTION_DONATIONS).document(donation.firestoreId)
            } else {
                firestore.collection(COLLECTION_DONATIONS).document()
            }
            val firestoreId = docRef.id

            val currentUser = auth?.currentUser
            val currentUid = currentUser?.uid ?: donation.userId
            val currentEmail = currentUser?.email ?: donation.userEmail

            val donationData = hashMapOf<String, Any?>(
                "id" to donation.id,
                "firestoreId" to firestoreId,
                "donorName" to donation.donorName,
                "phone" to donation.phone,
                "amount" to donation.amount,
                "status" to donation.status,
                "paymentMode" to donation.paymentMode,
                "paymentDate" to donation.paymentDate,
                "receiptNo" to donation.receiptNo,
                "notes" to donation.notes,
                "recordedById" to donation.recordedById,
                "recordedByName" to donation.recordedByName,
                "userId" to currentUid,
                "userEmail" to currentEmail,
                "createdAt" to if (donation.createdAt > 0) donation.createdAt else System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(donationData, SetOptions.merge()).await()
            Log.d(TAG, "Donation saved with ID: $firestoreId")
            Result.success(firestoreId)
        } catch (e: Exception) {
            logNotice("Creating donation", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve a Donation by ID.
     */
    override suspend fun getDonation(donationId: String): Result<Donation?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val doc = firestore.collection(COLLECTION_DONATIONS).document(donationId).get().await()
            if (doc.exists()) {
                Result.success(mapDocumentToDonation(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logNotice("Fetching donation $donationId", e)
            Result.failure(e)
        }
    }

    /**
     * Get all donations, optionally filtered or ordered by payment date.
     */
    override suspend fun getDonationsForSociety(societyId: String): Result<List<Donation>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val querySnapshot = firestore.collection(COLLECTION_DONATIONS)
                .orderBy("paymentDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val donations = querySnapshot.documents.mapNotNull { mapDocumentToDonation(it) }
            Result.success(donations)
        } catch (e: Exception) {
            logNotice("Getting donations", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow of donations.
     */
    override fun getDonationsFlow(societyId: String): Flow<List<Donation>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_DONATIONS)
            .orderBy("paymentDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logNotice("Donations flow listener", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { mapDocumentToDonation(it) } ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Query donations by donor phone number.
     */
    override suspend fun getDonationsByDonorPhone(phone: String): Result<List<Donation>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val querySnapshot = firestore.collection(COLLECTION_DONATIONS)
                .whereEqualTo("phone", phone.trim())
                .get()
                .await()

            val list = querySnapshot.documents.mapNotNull { mapDocumentToDonation(it) }
            Result.success(list)
        } catch (e: Exception) {
            logNotice("Fetching donations by phone $phone", e)
            Result.failure(e)
        }
    }

    /**
     * Update an entire donation.
     */
    override suspend fun updateDonation(donationId: String, donation: Donation): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val donationData = hashMapOf<String, Any?>(
                "donorName" to donation.donorName,
                "phone" to donation.phone,
                "amount" to donation.amount,
                "status" to donation.status,
                "paymentMode" to donation.paymentMode,
                "paymentDate" to donation.paymentDate,
                "receiptNo" to donation.receiptNo,
                "notes" to donation.notes,
                "recordedById" to donation.recordedById,
                "recordedByName" to donation.recordedByName,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_DONATIONS).document(donationId)
                .update(donationData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating donation $donationId", e)
            Result.failure(e)
        }
    }

    /**
     * Update only the payment status of a donation (e.g. from 'pending' to 'received').
     */
    override suspend fun updateDonationStatus(donationId: String, newStatus: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_DONATIONS).document(donationId)
                .update(mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating donation status $donationId", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a donation by Document ID.
     */
    override suspend fun deleteDonation(donationId: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_DONATIONS).document(donationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Deleting donation $donationId", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 3. USER COLLECTION CRUD OPERATIONS
    // =========================================================================

    /**
     * Create or update a User in Firestore.
     */
    override suspend fun createUser(user: User): Result<String> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val docRef = if (user.id.isNotBlank()) {
                firestore.collection(COLLECTION_USERS).document(user.id)
            } else {
                firestore.collection(COLLECTION_USERS).document()
            }
            val userId = docRef.id

            val userData = hashMapOf<String, Any?>(
                "id" to userId,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role,
                "pin" to user.pin,
                "isSuperAdmin" to user.isSuperAdmin,
                "userType" to user.userType,
                "societyId" to user.societyId,
                "societyName" to user.societyName,
                "flatOrUnitNo" to user.flatOrUnitNo,
                "isActive" to user.isActive,
                "createdAt" to if (user.createdAt > 0) user.createdAt else System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(userData, SetOptions.merge()).await()
            Log.d(TAG, "User saved with ID: $userId")
            Result.success(userId)
        } catch (e: Exception) {
            logNotice("Creating User", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve a User by ID.
     */
    override suspend fun getUser(userId: String): Result<User?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                Result.success(mapDocumentToUser(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logNotice("Fetching user $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow for a single User document.
     */
    override fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logNotice("Listening to user $userId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(mapDocumentToUser(snapshot))
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Retrieve all users, optionally filtered by Society ID.
     */
    override suspend fun getUsersForSociety(societyId: String): Result<List<User>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val query = if (societyId.isNotBlank()) {
                firestore.collection(COLLECTION_USERS).whereEqualTo("societyId", societyId)
            } else {
                firestore.collection(COLLECTION_USERS)
            }

            val snapshot = query.get().await()
            val list = snapshot.documents.mapNotNull { mapDocumentToUser(it) }
            Result.success(list)
        } catch (e: Exception) {
            logNotice("Fetching users", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow for users in a Society.
     */
    override fun getUsersFlow(societyId: String): Flow<List<User>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = if (societyId.isNotBlank()) {
            firestore.collection(COLLECTION_USERS).whereEqualTo("societyId", societyId)
        } else {
            firestore.collection(COLLECTION_USERS)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logNotice("Listening to users", error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { mapDocumentToUser(it) } ?: emptyList()
            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Find user by registered phone number.
     */
    override suspend fun getUserByPhone(phone: String): Result<User?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("phone", phone.trim())
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
            if (doc != null && doc.exists()) {
                Result.success(mapDocumentToUser(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logNotice("Fetching user by phone $phone", e)
            Result.failure(e)
        }
    }

    /**
     * Update entire user profile.
     */
    override suspend fun updateUser(userId: String, user: User): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = hashMapOf<String, Any?>(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role,
                "pin" to user.pin,
                "isSuperAdmin" to user.isSuperAdmin,
                "userType" to user.userType,
                "societyId" to user.societyId,
                "societyName" to user.societyName,
                "flatOrUnitNo" to user.flatOrUnitNo,
                "isActive" to user.isActive,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_USERS).document(userId)
                .update(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating user $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Allows a committee member to change their own PIN in Firestore.
     */
    override suspend fun updateUserPin(userId: String, newPin: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_USERS).document(userId)
                .update(mapOf(
                    "pin" to newPin.trim(),
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating PIN for user $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Super Admin power action: Reset a user's PIN to default ("1234").
     */
    override suspend fun resetUserPinToDefault(userId: String, defaultPin: String): Result<Unit> {
        return updateUserPin(userId, defaultPin)
    }

    /**
     * Delete a User document from Firestore.
     */
    override suspend fun deleteUser(userId: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_USERS).document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Deleting user $userId", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // MAPPER UTILITIES FOR FIRESTORE DOCUMENT CONVERSIONS
    // =========================================================================

    private fun mapDocumentToSociety(doc: DocumentSnapshot): Society? {
        return try {
            Society(
                id = doc.id,
                name = doc.getString("name") ?: doc.id,
                registrationNo = doc.getString("registrationNo") ?: "",
                upiId = doc.getString("upiId") ?: "",
                bankName = doc.getString("bankName") ?: "",
                accountNumber = doc.getString("accountNumber") ?: "",
                ifscCode = doc.getString("ifscCode") ?: "",
                branchName = doc.getString("branchName") ?: "",
                bankAccount = doc.getString("bankAccount") ?: "",
                address = doc.getString("address") ?: "",
                contactEmail = doc.getString("contactEmail") ?: "",
                contactPhone = doc.getString("contactPhone") ?: "",
                financialYear = doc.getString("financialYear") ?: "FY 2026-2027",
                openingBalance = doc.getDouble("openingBalance") ?: 0.0,
                presidentName = doc.getString("presidentName") ?: "",
                secretaryName = doc.getString("secretaryName") ?: "",
                treasurerName = doc.getString("treasurerName") ?: "",
                isPublic = doc.getBoolean("isPublic") ?: true,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notice mapping doc to Society: ${doc.id} - ${e.message}")
            null
        }
    }

    private fun mapDocumentToDonation(doc: DocumentSnapshot): Donation? {
        return try {
            Donation(
                id = doc.getLong("id") ?: 0L,
                donorName = doc.getString("donorName") ?: "Anonymous",
                phone = doc.getString("phone") ?: "",
                amount = doc.getDouble("amount") ?: 0.0,
                status = doc.getString("status") ?: "received",
                paymentMode = doc.getString("paymentMode") ?: "UPI/Online",
                paymentDate = doc.getLong("paymentDate") ?: System.currentTimeMillis(),
                receiptNo = doc.getString("receiptNo") ?: "RCP-${doc.id.take(6).uppercase()}",
                notes = doc.getString("notes") ?: "",
                recordedById = doc.getLong("recordedById") ?: 1L,
                recordedByName = doc.getString("recordedByName") ?: "Admin",
                userId = doc.getString("userId") ?: "",
                userEmail = doc.getString("userEmail") ?: "",
                firestoreId = doc.id,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notice mapping doc to Donation: ${doc.id} - ${e.message}")
            null
        }
    }

    private fun mapDocumentToUser(doc: DocumentSnapshot): User? {
        return try {
            User(
                id = doc.id,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                role = doc.getString("role") ?: "Member",
                pin = doc.getString("pin") ?: "1234",
                isSuperAdmin = doc.getBoolean("isSuperAdmin") ?: false,
                userType = doc.getString("userType") ?: User.USER_TYPE_COMMITTEE,
                societyId = doc.getString("societyId") ?: "",
                societyName = doc.getString("societyName") ?: "",
                flatOrUnitNo = doc.getString("flatOrUnitNo") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notice mapping doc to User: ${doc.id} - ${e.message}")
            null
        }
    }

    // =========================================================================
    // 4. AUDIT LOG CRUD OPERATIONS
    // =========================================================================

    /**
     * Store Audit Log in Firestore under 'audit_logs' collection.
     */
    override suspend fun createAuditLog(auditLog: com.example.data.model.AuditLog): Result<String> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val docRef = if (auditLog.firestoreId.isNotBlank()) {
                firestore.collection(COLLECTION_AUDIT_LOGS).document(auditLog.firestoreId)
            } else {
                firestore.collection(COLLECTION_AUDIT_LOGS).document()
            }
            val logId = docRef.id

            val logData = hashMapOf<String, Any?>(
                "transactionType" to auditLog.transactionType,
                "transactionId" to auditLog.transactionId,
                "transactionRef" to auditLog.transactionRef,
                "action" to auditLog.action,
                "reason" to auditLog.reason,
                "performedById" to auditLog.performedById,
                "performedByName" to auditLog.performedByName,
                "performedByRole" to auditLog.performedByRole,
                "oldValueSummary" to auditLog.oldValueSummary,
                "newValueSummary" to auditLog.newValueSummary,
                "societyId" to auditLog.societyId,
                "timestamp" to auditLog.timestamp,
                "localId" to auditLog.id,
                "createdAt" to System.currentTimeMillis()
            )

            docRef.set(logData, SetOptions.merge()).await()
            Log.d(TAG, "Audit log saved with ID: $logId")
            Result.success(logId)
        } catch (e: Exception) {
            logNotice("Saving audit log", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve all Audit Logs from Firestore, sorted by timestamp descending.
     */
    override suspend fun getAllAuditLogs(): Result<List<com.example.data.model.AuditLog>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = firestore.collection(COLLECTION_AUDIT_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val logs = snapshot.documents.mapNotNull { mapDocumentToAuditLog(it) }
            Result.success(logs)
        } catch (e: Exception) {
            logNotice("Fetching audit logs", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow of Audit Logs from Firestore.
     */
    override fun getAllAuditLogsFlow(): Flow<List<com.example.data.model.AuditLog>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_AUDIT_LOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logNotice("Listening to audit logs", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { mapDocumentToAuditLog(it) } ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    private fun mapDocumentToAuditLog(doc: DocumentSnapshot): com.example.data.model.AuditLog? {
        return try {
            com.example.data.model.AuditLog(
                id = doc.getLong("localId") ?: 0L,
                transactionType = doc.getString("transactionType") ?: "GENERAL",
                transactionId = doc.getLong("transactionId") ?: 0L,
                transactionRef = doc.getString("transactionRef") ?: "",
                action = doc.getString("action") ?: "AUDIT",
                reason = doc.getString("reason") ?: "No reason provided",
                performedById = doc.getLong("performedById") ?: 1L,
                performedByName = doc.getString("performedByName") ?: "Super Admin",
                performedByRole = doc.getString("performedByRole") ?: "Super Admin",
                oldValueSummary = doc.getString("oldValueSummary") ?: "",
                newValueSummary = doc.getString("newValueSummary") ?: "",
                societyId = doc.getString("societyId") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                firestoreId = doc.id
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notice mapping audit log doc: ${doc.id} - ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "FirestoreRepository"
        const val COLLECTION_SOCIETIES = "societies"
        const val COLLECTION_DONATIONS = "donations"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_AUDIT_LOGS = "audit_logs"
    }
}
