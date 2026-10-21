package com.example.data.remote

import android.util.Log
import com.example.data.local.PreloadedData
import com.example.data.model.AuditLog
import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.Pledge
import com.example.data.model.Society
import com.example.data.model.SocietyInfo
import com.example.data.model.Transaction
import com.example.data.model.UserSubscription
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore Service for Society Ledger.
 *
 * All primary collections (members, donations, expenses, user transactions, pledges,
 * committee members, audit logs) are nested under the parent society document, which is
 * identified and represented by the society's name:
 *
 *   /societies/{societyName}/members/{memberId}
 *   /societies/{societyName}/donations/{donationId}
 *   /societies/{societyName}/expenses/{expenseId}
 *   /societies/{societyName}/transactions/{transactionId}
 *   /societies/{societyName}/pledges/{pledgeId}
 *   /societies/{societyName}/committee_members/{memberId}
 *   /societies/{societyName}/audit_logs/{logId}
 */
class FirestoreService {

    @Volatile
    var activeSocietyName: String = ""

    @Volatile
    private var isCloudAccessRestricted = false

    @Volatile
    var lastCloudError: String? = null
        private set

    fun clearLastError() {
        lastCloudError = null
    }

    fun resetCloudAccessStatus() {
        isCloudAccessRestricted = false
    }

    fun isCloudAvailable(): Boolean = (auth?.currentUser != null) || !isCloudAccessRestricted

    private fun handleFirestoreError(action: String, e: Exception) {
        val detail = if (e is FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "PERMISSION_DENIED: Cloud security rules blocked access. Please check authorization."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "NETWORK_UNAVAILABLE: Could not reach cloud servers. Check internet connection."
                else -> "Cloud Error (${e.code}): ${e.localizedMessage ?: e.message}"
            }
        } else {
            e.localizedMessage ?: e.message ?: "Unknown cloud error"
        }
        lastCloudError = "$action: $detail"
        if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            if (auth?.currentUser == null) {
                isCloudAccessRestricted = true
            }
            Log.w(TAG, "Notice: $action - $detail. Active user: ${auth?.currentUser?.email ?: "none"}. Local database active.")
        } else {
            Log.w(TAG, "Notice: $action - $detail. Local database active.")
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Could not initialize FirebaseFirestore: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Could not initialize FirebaseAuth: ${e.message}")
            null
        }
    }

    /**
     * Sanitizes society name so it forms a valid Firestore document ID.
     * Replaces slashes with hyphens.
     */
    fun sanitizeSocietyDocId(raw: String?): String {
        val trimmed = raw?.trim() ?: ""
        val clean = trimmed.replace("/", "-").replace("\\", "-")
        return if (clean.isNotBlank()) clean else "default_society"
    }

    /**
     * Resolves the target society document ID from an explicit parameter,
     * the activeSocietyName property, or the default society name.
     */
    fun resolveSocietyDocId(explicitName: String? = null): String {
        val candidate = explicitName?.takeIf { it.isNotBlank() }
            ?: activeSocietyName.takeIf { it.isNotBlank() }
            ?: "default_society"
        return sanitizeSocietyDocId(candidate)
    }

    /**
     * Ensures that the parent society document exists in the 'societies' collection
     * so that it displays properly (not as an italic ghost document) in the Firebase Console.
     */
    suspend fun ensureSocietyDocExists(societyDocId: String) {
        val db = firestore ?: return
        try {
            val socRef = db.collection(COLLECTION_SOCIETIES).document(societyDocId)
            socRef.set(
                mapOf(
                    "id" to societyDocId,
                    "name" to societyDocId,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Notice: ensureSocietyDocExists for $societyDocId: ${e.message}")
        }
    }

    /**
     * Helper to get a subcollection reference under the target society document.
     */
    private fun getSocietySubcollection(subcollectionName: String, societyName: String? = null): CollectionReference? {
        val db = firestore ?: return null
        val docId = resolveSocietyDocId(societyName)
        return db.collection(COLLECTION_SOCIETIES).document(docId).collection(subcollectionName)
    }

    /**
     * Ensures the user is authenticated with Firebase (anonymously or via credential).
     */
    suspend fun ensureAuthenticated(): Boolean {
        val a = auth ?: return false
        if (a.currentUser != null) {
            isCloudAccessRestricted = false
            return true
        }
        return try {
            a.signInAnonymously().await()
            isCloudAccessRestricted = false
            Log.d(TAG, "Firebase signed in anonymously: ${a.currentUser?.uid}")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous auth not enabled or failed, continuing: ${e.message}")
            false
        }
    }

    /**
     * Retrieves the currently active Firebase Auth user identifier and email.
     */
    fun getCurrentFirebaseUser(): Pair<String, String>? {
        return try {
            val user = auth?.currentUser
            if (user != null) {
                Pair(user.uid, user.email ?: "")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Performs a direct connection and permission test against Firestore.
     * Also verifies that the parent society document is accessible.
     */
    suspend fun testCloudConnection(): Pair<Boolean, String> {
        val db = firestore ?: return Pair(false, "Cloud synchronization service not initialized.")
        resetCloudAccessStatus()
        clearLastError()
        return try {
            ensureAuthenticated()
            val user = auth?.currentUser
            val authStatus = if (user != null) {
                if (user.isAnonymous) "Anonymous UID (${user.uid.take(8)})"
                else "Authenticated as ${user.email}"
            } else "Unauthenticated"

            val socDocId = resolveSocietyDocId()
            ensureSocietyDocExists(socDocId)

            val testDoc = db.collection(COLLECTION_SOCIETIES).document(socDocId)
            testDoc.set(
                mapOf(
                    "name" to socDocId,
                    "lastConnectionTest" to System.currentTimeMillis(),
                    "status" to "online"
                ),
                SetOptions.merge()
            ).await()

            Pair(true, "Connected to Cloud successfully! Society: $socDocId ($authStatus)")
        } catch (e: Exception) {
            handleFirestoreError("Connection test", e)
            val err = lastCloudError ?: e.message ?: "Connection test failed"
            val rawMsg = if (e is FirebaseFirestoreException) " [code=${e.code}, msg=${e.message}]" else " [${e.javaClass.simpleName}: ${e.message}]"
            Pair(false, "$err$rawMsg")
        }
    }

    // =========================================================================
    // 1. EXPENSES (Nested under /societies/{societyName}/expenses)
    // =========================================================================

    suspend fun saveExpense(
        expense: Expense,
        fallbackUserId: String = "",
        fallbackUserEmail: String = "",
        societyName: String = ""
    ): String? {
        val db = firestore ?: return null
        if (expense.amount <= 0) {
            Log.w(TAG, "Validation failed for Firestore saveExpense: Amount must be greater than 0 (${expense.amount})")
            return null
        }
        if (expense.title.isBlank()) {
            Log.w(TAG, "Validation failed for Firestore saveExpense: Title cannot be blank")
            return null
        }
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else expense.societyId)
            ensureSocietyDocExists(socDocId)

            val expensesCol = db.collection(COLLECTION_SOCIETIES).document(socDocId).collection(COLLECTION_EXPENSES)
            val docRef = if (expense.firestoreId.isNotBlank()) {
                expensesCol.document(expense.firestoreId)
            } else {
                expensesCol.document()
            }

            val (firebaseUid, firebaseEmail) = getCurrentFirebaseUser() ?: Pair(fallbackUserId, fallbackUserEmail)
            val effectiveUserId = firebaseUid.ifBlank { fallbackUserId.ifBlank { "anonymous_user" } }
            val effectiveUserEmail = firebaseEmail.ifBlank { fallbackUserEmail.ifBlank { "committee@society.org" } }

            val expenseData = hashMapOf<String, Any?>(
                "title" to expense.title,
                "category" to expense.category,
                "amount" to expense.amount,
                "expenseDate" to expense.expenseDate,
                "paidTo" to expense.paidTo,
                "paymentMode" to expense.paymentMode,
                "receiptUrl" to expense.receiptUrl,
                "paidByCommitteeMemberId" to expense.paidByCommitteeMemberId,
                "recordedById" to expense.recordedById,
                "recordedByName" to expense.recordedByName,
                "userId" to (if (expense.userId.isNotBlank()) expense.userId else effectiveUserId),
                "userEmail" to (if (expense.userEmail.isNotBlank()) expense.userEmail else effectiveUserEmail),
                "localId" to expense.id,
                "societyId" to socDocId,
                "societyName" to socDocId,
                "isModified" to expense.isModified,
                "modificationReason" to expense.modificationReason,
                "modifiedAt" to expense.modifiedAt,
                "modifiedByName" to expense.modifiedByName,
                "isDeleted" to expense.isDeleted,
                "deletionReason" to expense.deletionReason,
                "deletedAt" to expense.deletedAt,
                "deletedByName" to expense.deletedByName,
                "createdAt" to expense.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(expenseData, SetOptions.merge()).await()
            Log.d(TAG, "Expense saved to Firestore under societies/$socDocId/expenses/${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError("Saving expense", e)
            null
        }
    }

    suspend fun updateExpense(expense: Expense, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else expense.societyId)
            if (expense.firestoreId.isBlank()) {
                saveExpense(expense, societyName = socDocId)
                return true
            }

            val (firebaseUid, firebaseEmail) = getCurrentFirebaseUser() ?: Pair(expense.userId, expense.userEmail)
            val updateData = hashMapOf<String, Any?>(
                "title" to expense.title,
                "category" to expense.category,
                "amount" to expense.amount,
                "expenseDate" to expense.expenseDate,
                "paidTo" to expense.paidTo,
                "paymentMode" to expense.paymentMode,
                "receiptUrl" to expense.receiptUrl,
                "recordedById" to expense.recordedById,
                "recordedByName" to expense.recordedByName,
                "userId" to if (expense.userId.isNotBlank()) expense.userId else firebaseUid,
                "userEmail" to if (expense.userEmail.isNotBlank()) expense.userEmail else firebaseEmail,
                "isModified" to expense.isModified,
                "modificationReason" to expense.modificationReason,
                "modifiedAt" to expense.modifiedAt,
                "modifiedByName" to expense.modifiedByName,
                "isDeleted" to expense.isDeleted,
                "deletionReason" to expense.deletionReason,
                "deletedAt" to expense.deletedAt,
                "deletedByName" to expense.deletedByName,
                "societyId" to socDocId,
                "societyName" to socDocId,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_EXPENSES).document(expense.firestoreId)
                .update(updateData).await()
            Log.d(TAG, "Expense updated in Firestore: societies/$socDocId/expenses/${expense.firestoreId}")
            true
        } catch (e: Exception) {
            handleFirestoreError("Updating expense", e)
            false
        }
    }

    suspend fun deleteExpense(firestoreId: String, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        if (firestoreId.isBlank()) return true
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_EXPENSES).document(firestoreId).delete().await()
            // Also clean legacy flat collection if present
            try {
                db.collection(COLLECTION_EXPENSES).document(firestoreId).delete().await()
            } catch (_: Exception) {}
            Log.d(TAG, "Expense deleted from Firestore: $firestoreId")
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting expense", e)
            false
        }
    }

    suspend fun fetchAllExpenses(societyName: String = ""): List<Expense> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_EXPENSES, societyName) ?: return emptyList()
            val snapshot = targetCol.get().await()
            val results = snapshot.documents.mapNotNull { doc -> mapDocToExpense(doc) }
            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching all expenses", e)
            emptyList()
        }
    }

    fun getExpensesFlow(societyName: String = ""): Flow<List<Expense>> = callbackFlow {
        val targetCol = getSocietySubcollection(COLLECTION_EXPENSES, societyName)
        if (targetCol == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = targetCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Notice: Firestore expenses snapshot error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val expenses = snapshot.documents.mapNotNull { doc -> mapDocToExpense(doc) }
                trySend(expenses)
            }
        }

        awaitClose { listener.remove() }
    }

    // =========================================================================
    // 2. DONATIONS (Nested under /societies/{societyName}/donations)
    // =========================================================================

    suspend fun saveDonation(
        donation: Donation,
        fallbackUserId: String = "",
        fallbackUserEmail: String = "",
        societyName: String = ""
    ): String? {
        val db = firestore ?: return null
        if (donation.amount <= 0) {
            Log.w(TAG, "Validation failed for Firestore saveDonation: Amount must be greater than 0 (${donation.amount})")
            return null
        }
        if (donation.donorName.isBlank()) {
            Log.w(TAG, "Validation failed for Firestore saveDonation: Donor name cannot be blank")
            return null
        }
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else donation.societyId)
            ensureSocietyDocExists(socDocId)

            val donationsCol = db.collection(COLLECTION_SOCIETIES).document(socDocId).collection(COLLECTION_DONATIONS)
            val docRef = if (donation.firestoreId.isNotBlank()) {
                donationsCol.document(donation.firestoreId)
            } else {
                donationsCol.document()
            }

            val (firebaseUid, firebaseEmail) = getCurrentFirebaseUser() ?: Pair(fallbackUserId, fallbackUserEmail)
            val effectiveUserId = firebaseUid.ifBlank { fallbackUserId.ifBlank { "anonymous_user" } }
            val effectiveUserEmail = firebaseEmail.ifBlank { fallbackUserEmail.ifBlank { "committee@society.org" } }

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
                "userId" to (if (donation.userId.isNotBlank()) donation.userId else effectiveUserId),
                "userEmail" to (if (donation.userEmail.isNotBlank()) donation.userEmail else effectiveUserEmail),
                "localId" to donation.id,
                "societyId" to socDocId,
                "societyName" to socDocId,
                "isModified" to donation.isModified,
                "modificationReason" to donation.modificationReason,
                "modifiedAt" to donation.modifiedAt,
                "modifiedByName" to donation.modifiedByName,
                "isDeleted" to donation.isDeleted,
                "deletionReason" to donation.deletionReason,
                "deletedAt" to donation.deletedAt,
                "deletedByName" to donation.deletedByName,
                "createdAt" to donation.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(donationData, SetOptions.merge()).await()
            Log.d(TAG, "Donation saved to Firestore under societies/$socDocId/donations/${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError("Saving donation", e)
            null
        }
    }

    suspend fun updateDonation(donation: Donation, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else donation.societyId)
            if (donation.firestoreId.isBlank()) {
                saveDonation(donation, societyName = socDocId)
                return true
            }

            val (firebaseUid, firebaseEmail) = getCurrentFirebaseUser() ?: Pair(donation.userId, donation.userEmail)
            val updateData = hashMapOf<String, Any?>(
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
                "userId" to if (donation.userId.isNotBlank()) donation.userId else firebaseUid,
                "userEmail" to if (donation.userEmail.isNotBlank()) donation.userEmail else firebaseEmail,
                "isModified" to donation.isModified,
                "modificationReason" to donation.modificationReason,
                "modifiedAt" to donation.modifiedAt,
                "modifiedByName" to donation.modifiedByName,
                "isDeleted" to donation.isDeleted,
                "deletionReason" to donation.deletionReason,
                "deletedAt" to donation.deletedAt,
                "deletedByName" to donation.deletedByName,
                "societyId" to socDocId,
                "societyName" to socDocId,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_DONATIONS).document(donation.firestoreId)
                .update(updateData).await()
            Log.d(TAG, "Donation updated in Firestore: societies/$socDocId/donations/${donation.firestoreId}")
            true
        } catch (e: Exception) {
            handleFirestoreError("Updating donation", e)
            false
        }
    }

    suspend fun deleteDonation(firestoreId: String, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        if (firestoreId.isBlank()) return true
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_DONATIONS).document(firestoreId).delete().await()
            try {
                db.collection(COLLECTION_DONATIONS).document(firestoreId).delete().await()
            } catch (_: Exception) {}
            Log.d(TAG, "Donation deleted from Firestore: $firestoreId")
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting donation", e)
            false
        }
    }

    suspend fun fetchAllDonations(societyName: String = ""): List<Donation> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_DONATIONS, societyName) ?: return emptyList()
            val snapshot = targetCol.get().await()
            val results = snapshot.documents.mapNotNull { doc -> mapDocToDonation(doc) }
            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching all donations", e)
            emptyList()
        }
    }

    fun getDonationsFlow(societyName: String = ""): Flow<List<Donation>> = callbackFlow {
        val targetCol = getSocietySubcollection(COLLECTION_DONATIONS, societyName)
        if (targetCol == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = targetCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Notice: Firestore donations snapshot error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val donations = snapshot.documents.mapNotNull { doc -> mapDocToDonation(doc) }
                trySend(donations)
            }
        }

        awaitClose { listener.remove() }
    }

    // =========================================================================
    // 3. MEMBERS (Nested under /societies/{societyName}/members)
    // =========================================================================

    suspend fun saveMember(member: Member, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            ensureSocietyDocExists(socDocId)

            val docId = if (member.id > 0) {
                "member_${member.id}"
            } else if (member.email.isNotBlank()) {
                "member_${member.email.replace("@", "_").replace(".", "_")}"
            } else {
                "member_${System.currentTimeMillis()}"
            }
            val memberData = hashMapOf<String, Any?>(
                "id" to member.id,
                "name" to member.name,
                "role" to member.role,
                "phone" to member.phone,
                "email" to member.email,
                "flatNo" to member.flatNo,
                "pin" to member.pin,
                "isSuperAdmin" to member.isSuperAdmin,
                "societyId" to socDocId,
                "societyName" to socDocId,
                "createdAt" to member.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_MEMBERS).document(docId)
                .set(memberData, SetOptions.merge()).await()
            Log.d(TAG, "Member synced to Firestore under societies/$socDocId/members/$docId")
            true
        } catch (e: Exception) {
            handleFirestoreError("Saving member", e)
            false
        }
    }

    suspend fun deleteMember(memberId: Long, memberEmail: String = "", societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            val docId = if (memberId > 0) {
                "member_${memberId}"
            } else if (memberEmail.isNotBlank()) {
                "member_${memberEmail.replace("@", "_").replace(".", "_")}"
            } else return false

            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_MEMBERS).document(docId).delete().await()
            try {
                db.collection(COLLECTION_MEMBERS).document(docId).delete().await()
            } catch (_: Exception) {}
            Log.d(TAG, "Member deleted from Firestore: $docId")
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting member", e)
            false
        }
    }

    suspend fun fetchAllMembers(societyName: String = ""): List<Member> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_MEMBERS, societyName) ?: return emptyList()
            val snapshot = targetCol.get().await()
            val results = snapshot.documents.mapNotNull { doc -> mapDocToMember(doc) }
            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching all members", e)
            emptyList()
        }
    }

    // =========================================================================
    // 4. USER TRANSACTIONS (Nested under /societies/{societyName}/transactions)
    // =========================================================================

    suspend fun saveTransaction(
        transaction: Transaction,
        fallbackUserId: String = "",
        fallbackUserEmail: String = "",
        societyName: String = ""
    ): String? {
        val db = firestore ?: return null
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else transaction.societyId)
            ensureSocietyDocExists(socDocId)

            val txCol = db.collection(COLLECTION_SOCIETIES).document(socDocId).collection(COLLECTION_TRANSACTIONS)
            val docRef = if (transaction.firestoreId.isNotBlank()) {
                txCol.document(transaction.firestoreId)
            } else {
                txCol.document()
            }

            val (firebaseUid, firebaseEmail) = getCurrentFirebaseUser() ?: Pair(fallbackUserId, fallbackUserEmail)
            val effectiveUserId = firebaseUid.ifBlank { fallbackUserId.ifBlank { "anonymous_user" } }
            val effectiveUserEmail = firebaseEmail.ifBlank { fallbackUserEmail.ifBlank { "committee@society.org" } }

            val data = transaction.toFirestoreMap().apply {
                put("userId", if (transaction.userId.isNotBlank()) transaction.userId else effectiveUserId)
                put("userEmail", if (transaction.userEmail.isNotBlank()) transaction.userEmail else effectiveUserEmail)
                put("societyId", socDocId)
                put("societyName", socDocId)
                put("updatedAt", System.currentTimeMillis())
            }

            docRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Transaction successfully saved to Firestore under societies/$socDocId/transactions/${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError("Saving transaction", e)
            null
        }
    }

    suspend fun updateTransaction(transaction: Transaction, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else transaction.societyId)
            if (transaction.firestoreId.isBlank()) {
                saveTransaction(transaction, societyName = socDocId)
                return true
            }
            val targetDoc = db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_TRANSACTIONS).document(transaction.firestoreId)
            val data = transaction.toFirestoreMap().apply {
                put("societyId", socDocId)
                put("societyName", socDocId)
                put("updatedAt", System.currentTimeMillis())
            }
            targetDoc.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Transaction updated in Firestore under societies/$socDocId/transactions/${transaction.firestoreId}")
            true
        } catch (e: Exception) {
            handleFirestoreError("Updating transaction", e)
            false
        }
    }

    suspend fun deleteTransaction(firestoreId: String, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        if (firestoreId.isBlank()) return true
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_TRANSACTIONS).document(firestoreId).delete().await()
            try {
                db.collection(COLLECTION_TRANSACTIONS).document(firestoreId).delete().await()
            } catch (_: Exception) {}
            Log.d(TAG, "Transaction deleted in Firestore: $firestoreId")
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting transaction", e)
            false
        }
    }

    suspend fun fetchAllTransactions(societyName: String = ""): List<Transaction> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_TRANSACTIONS, societyName) ?: return emptyList()
            val snapshot = targetCol.get().await()
            val results = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Transaction.fromFirestoreDoc(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }
            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching transactions", e)
            emptyList()
        }
    }

    // =========================================================================
    // 5. SOCIETY MASTER RECORDS (/societies/{societyName})
    // =========================================================================

    suspend fun saveSocietyInfo(societyInfo: SocietyInfo): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            activeSocietyName = societyInfo.name
            val docId = resolveSocietyDocId(societyInfo.name)

            val societyData = hashMapOf<String, Any?>(
                "id" to docId,
                "name" to societyInfo.name,
                "registrationNo" to societyInfo.registrationNo,
                "upiId" to societyInfo.upiId,
                "bankName" to societyInfo.bankName,
                "accountNumber" to societyInfo.accountNumber,
                "ifscCode" to societyInfo.ifscCode,
                "branchName" to societyInfo.branchName,
                "bankAccount" to societyInfo.fullBankDetails,
                "address" to societyInfo.address,
                "city" to societyInfo.city,
                "state" to societyInfo.state,
                "pinCode" to societyInfo.pinCode,
                "contactEmail" to societyInfo.contactEmail,
                "adminEmail" to societyInfo.adminEmail,
                "contactPhone" to societyInfo.contactPhone,
                "financialYear" to societyInfo.financialYear,
                "openingBalance" to societyInfo.openingBalance,
                "presidentName" to societyInfo.presidentName,
                "secretaryName" to societyInfo.secretaryName,
                "treasurerName" to societyInfo.treasurerName,
                "securityGatePhone" to societyInfo.securityGatePhone,
                "maintenanceLeadPhone" to societyInfo.maintenanceLeadPhone,
                "plumberPhone" to societyInfo.plumberPhone,
                "emergencyHelplinePhone" to societyInfo.emergencyHelplinePhone,
                "nestedCollections" to FieldValue.delete(),
                "schemaVersion" to FieldValue.delete(),
                "hierarchyPath" to FieldValue.delete(),
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_SOCIETIES).document(docId).set(societyData, SetOptions.merge()).await()
            Log.d(TAG, "Society master info saved in Firestore under societies/$docId (represented with name)")
            true
        } catch (e: Exception) {
            handleFirestoreError("Saving society info", e)
            false
        }
    }

    suspend fun fetchAllSocieties(): List<SocietyInfo> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val snapshot = db.collection(COLLECTION_SOCIETIES).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    SocietyInfo(
                        name = doc.getString("name") ?: doc.id,
                        registrationNo = doc.getString("registrationNo") ?: "",
                        upiId = doc.getString("upiId") ?: "",
                        bankName = doc.getString("bankName") ?: "",
                        accountNumber = doc.getString("accountNumber") ?: "",
                        ifscCode = doc.getString("ifscCode") ?: "",
                        branchName = doc.getString("branchName") ?: "",
                        bankAccount = doc.getString("bankAccount") ?: "",
                        address = doc.getString("address") ?: "",
                        city = doc.getString("city") ?: "",
                        state = doc.getString("state") ?: "",
                        pinCode = doc.getString("pinCode") ?: "",
                        contactEmail = doc.getString("contactEmail") ?: "",
                        adminEmail = doc.getString("adminEmail") ?: "",
                        contactPhone = doc.getString("contactPhone") ?: "",
                        financialYear = doc.getString("financialYear") ?: "FY 2026-2027",
                        openingBalance = doc.getDouble("openingBalance") ?: 0.0,
                        presidentName = doc.getString("presidentName") ?: "",
                        secretaryName = doc.getString("secretaryName") ?: "",
                        treasurerName = doc.getString("treasurerName") ?: "",
                        securityGatePhone = doc.getString("securityGatePhone") ?: "",
                        maintenanceLeadPhone = doc.getString("maintenanceLeadPhone") ?: "",
                        plumberPhone = doc.getString("plumberPhone") ?: "",
                        emergencyHelplinePhone = doc.getString("emergencyHelplinePhone") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            handleFirestoreError("Fetching societies", e)
            emptyList()
        }
    }

    suspend fun saveSociety(society: Society): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            activeSocietyName = society.name
            val docId = resolveSocietyDocId(society.name)

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
                "city" to society.city,
                "state" to society.state,
                "pinCode" to society.pinCode,
                "contactEmail" to society.contactEmail,
                "adminEmail" to society.adminEmail,
                "contactPhone" to society.contactPhone,
                "financialYear" to society.financialYear,
                "openingBalance" to society.openingBalance,
                "presidentName" to society.presidentName,
                "secretaryName" to society.secretaryName,
                "treasurerName" to society.treasurerName,
                "isPublic" to society.isPublic,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_SOCIETIES).document(docId).set(societyData, SetOptions.merge()).await()
            Log.d(TAG, "Society directory entry synced to Firestore under societies/$docId (represented with name)")
            true
        } catch (e: Exception) {
            handleFirestoreError("Saving society directory", e)
            false
        }
    }

    suspend fun fetchAllDirectorySocieties(): List<Society> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val snapshot = db.collection(COLLECTION_SOCIETIES).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val docId = doc.getString("id") ?: doc.id
                    Society(
                        id = docId,
                        name = doc.getString("name") ?: doc.id,
                        registrationNo = doc.getString("registrationNo") ?: "",
                        upiId = doc.getString("upiId") ?: "",
                        bankName = doc.getString("bankName") ?: "",
                        accountNumber = doc.getString("accountNumber") ?: "",
                        ifscCode = doc.getString("ifscCode") ?: "",
                        branchName = doc.getString("branchName") ?: "",
                        bankAccount = doc.getString("bankAccount") ?: "",
                        address = doc.getString("address") ?: "",
                        city = doc.getString("city") ?: "",
                        state = doc.getString("state") ?: "",
                        pinCode = doc.getString("pinCode") ?: "",
                        contactEmail = doc.getString("contactEmail") ?: "",
                        adminEmail = doc.getString("adminEmail") ?: "",
                        contactPhone = doc.getString("contactPhone") ?: "",
                        financialYear = doc.getString("financialYear") ?: "FY 2026-2027",
                        openingBalance = doc.getDouble("openingBalance") ?: 0.0,
                        presidentName = doc.getString("presidentName") ?: "",
                        secretaryName = doc.getString("secretaryName") ?: "",
                        treasurerName = doc.getString("treasurerName") ?: "",
                        isPublic = doc.getBoolean("isPublic") ?: true
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            handleFirestoreError("Fetching directory societies", e)
            emptyList()
        }
    }

    suspend fun deleteSocietyInfo(societyIdOrName: String): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val docId = resolveSocietyDocId(societyIdOrName)
            val docRef = db.collection(COLLECTION_SOCIETIES).document(docId)

            // Remove nested subcollection documents
            val subcollections = listOf(
                COLLECTION_TRANSACTIONS,
                COLLECTION_EXPENSES,
                COLLECTION_DONATIONS,
                COLLECTION_MEMBERS,
                COLLECTION_AUDIT_LOGS,
                COLLECTION_PLEDGES,
                "committee_members"
            )
            for (sub in subcollections) {
                try {
                    val snapshot = docRef.collection(sub).get().await()
                    for (d in snapshot.documents) {
                        try { d.reference.delete().await() } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }

            docRef.delete().await()
            Log.d(TAG, "Society $docId deleted from Firestore")
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting society", e)
            false
        }
    }

    // =========================================================================
    // 6. AUDIT LOGS (Nested under /societies/{societyName}/audit_logs)
    // =========================================================================

    suspend fun saveAuditLog(auditLog: AuditLog, societyName: String = ""): String? {
        val db = firestore ?: return null
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else auditLog.societyId)
            ensureSocietyDocExists(socDocId)

            val col = db.collection(COLLECTION_SOCIETIES).document(socDocId).collection(COLLECTION_AUDIT_LOGS)
            val docRef = if (auditLog.firestoreId.isNotBlank()) {
                col.document(auditLog.firestoreId)
            } else {
                col.document()
            }

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
                "societyId" to socDocId,
                "societyName" to socDocId,
                "timestamp" to auditLog.timestamp,
                "localId" to auditLog.id,
                "createdAt" to System.currentTimeMillis()
            )

            docRef.set(logData, SetOptions.merge()).await()
            Log.d(TAG, "Audit log saved to Firestore under societies/$socDocId/audit_logs/${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError("Saving audit log", e)
            null
        }
    }

    suspend fun fetchAuditLogs(societyName: String = ""): List<AuditLog> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_AUDIT_LOGS, societyName) ?: return emptyList()
            val snapshot = targetCol.orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            var results = snapshot.documents.mapNotNull { doc -> mapDocToAuditLog(doc) }

            // Fallback to legacy root collection
            if (results.isEmpty()) {
                val flatSnapshot = db.collection(COLLECTION_AUDIT_LOGS)
                    .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
                results = flatSnapshot.documents.mapNotNull { doc -> mapDocToAuditLog(doc) }
            }

            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching audit logs", e)
            emptyList()
        }
    }

    fun getAuditLogsFlow(societyName: String = ""): Flow<List<AuditLog>> = callbackFlow {
        val targetCol = getSocietySubcollection(COLLECTION_AUDIT_LOGS, societyName)
        if (targetCol == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = targetCol.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Notice: Firestore audit logs snapshot error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val logs = snapshot.documents.mapNotNull { doc -> mapDocToAuditLog(doc) }
                    trySend(logs)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun fetchModifiedAndDeletedTransactions(societyName: String = ""): Pair<List<Donation>, List<Expense>> {
        return try {
            val allDonations = fetchAllDonations(societyName)
            val auditedDonations = allDonations.filter { it.isModified || it.isDeleted }

            val allExpenses = fetchAllExpenses(societyName)
            val auditedExpenses = allExpenses.filter { it.isModified || it.isDeleted }

            Pair(auditedDonations, auditedExpenses)
        } catch (e: Exception) {
            handleFirestoreError("Fetching modified/deleted transactions", e)
            Pair(emptyList(), emptyList())
        }
    }

    // =========================================================================
    // 7. PLEDGES (Nested under /societies/{societyName}/pledges)
    // =========================================================================

    suspend fun savePledge(
        pledge: Pledge,
        fallbackUserId: String = "",
        fallbackUserEmail: String = "",
        societyName: String = ""
    ): String? {
        val db = firestore ?: return null
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else pledge.societyId)
            ensureSocietyDocExists(socDocId)

            val col = db.collection(COLLECTION_SOCIETIES).document(socDocId).collection(COLLECTION_PLEDGES)
            val docRef = if (pledge.firestoreId.isNotBlank()) {
                col.document(pledge.firestoreId)
            } else {
                col.document()
            }

            val (firebaseUid, firebaseEmail) = getCurrentFirebaseUser() ?: Pair(fallbackUserId, fallbackUserEmail)
            val effectiveUserId = firebaseUid.ifBlank { fallbackUserId.ifBlank { "anonymous_user" } }
            val effectiveUserEmail = firebaseEmail.ifBlank { fallbackUserEmail.ifBlank { "committee@society.org" } }

            val data = pledge.toFirestoreMap().apply {
                put("userId", if (pledge.userId.isNotBlank()) pledge.userId else effectiveUserId)
                put("userEmail", if (pledge.userEmail.isNotBlank()) pledge.userEmail else effectiveUserEmail)
                put("societyId", socDocId)
                put("societyName", socDocId)
                put("updatedAt", System.currentTimeMillis())
            }

            docRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Pledge saved to Firestore under societies/$socDocId/pledges/${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError("Saving pledge", e)
            null
        }
    }

    suspend fun updatePledge(pledge: Pledge, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(if (societyName.isNotBlank()) societyName else pledge.societyId)
            if (pledge.firestoreId.isBlank()) {
                savePledge(pledge, societyName = socDocId)
                return true
            }
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_PLEDGES).document(pledge.firestoreId)
                .set(pledge.toFirestoreMap(), SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError("Updating pledge", e)
            false
        }
    }

    suspend fun deletePledge(firestoreId: String, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        if (firestoreId.isBlank()) return true
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_PLEDGES).document(firestoreId).delete().await()
            try {
                db.collection(COLLECTION_PLEDGES).document(firestoreId).delete().await()
            } catch (_: Exception) {}
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting pledge", e)
            false
        }
    }

    suspend fun fetchAllPledges(societyName: String = ""): List<Pledge> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_PLEDGES, societyName) ?: return emptyList()
            val snapshot = targetCol.get().await()
            var results = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Pledge.fromFirestoreDoc(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }

            // Fallback to legacy root collection
            if (results.isEmpty()) {
                val flatSnapshot = db.collection(COLLECTION_PLEDGES).get().await()
                results = flatSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Pledge.fromFirestoreDoc(doc.id, data)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching pledges", e)
            emptyList()
        }
    }

    // =========================================================================
    // 8. COMMITTEE MEMBERS (Nested under /societies/{societyName}/committee_members)
    // =========================================================================

    suspend fun saveCommitteeMember(member: CommitteeMember, societyName: String = ""): String? {
        val db = firestore ?: return null
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            ensureSocietyDocExists(socDocId)

            val docId = if (member.firestoreId.isNotBlank()) member.firestoreId else "cm_${member.id}"
            val docRef = db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_COMMITTEE_MEMBERS).document(docId)
            docRef.set(member.toFirestoreMap(), SetOptions.merge()).await()
            Log.d(TAG, "Committee member saved under societies/$socDocId/committee_members/$docId")
            docId
        } catch (e: Exception) {
            handleFirestoreError("Saving committee member", e)
            null
        }
    }

    suspend fun updateCommitteeMember(member: CommitteeMember, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            val docId = if (member.firestoreId.isNotBlank()) member.firestoreId else "cm_${member.id}"
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_COMMITTEE_MEMBERS).document(docId)
                .set(member.toFirestoreMap(), SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError("Updating committee member", e)
            false
        }
    }

    suspend fun deleteCommitteeMember(firestoreId: String, societyName: String = ""): Boolean {
        val db = firestore ?: return false
        if (firestoreId.isBlank()) return true
        return try {
            ensureAuthenticated()
            val socDocId = resolveSocietyDocId(societyName)
            db.collection(COLLECTION_SOCIETIES).document(socDocId)
                .collection(COLLECTION_COMMITTEE_MEMBERS).document(firestoreId).delete().await()
            try {
                db.collection(COLLECTION_COMMITTEE_MEMBERS).document(firestoreId).delete().await()
            } catch (_: Exception) {}
            true
        } catch (e: Exception) {
            handleFirestoreError("Deleting committee member", e)
            false
        }
    }

    suspend fun fetchAllCommitteeMembers(societyName: String = ""): List<CommitteeMember> {
        val db = firestore ?: return emptyList()
        if (isCloudAccessRestricted && auth?.currentUser == null) return emptyList()
        return try {
            ensureAuthenticated()
            val targetCol = getSocietySubcollection(COLLECTION_COMMITTEE_MEMBERS, societyName) ?: return emptyList()
            val snapshot = targetCol.get().await()
            var results = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    CommitteeMember.fromFirestoreDoc(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }

            // Fallback to legacy root collection
            if (results.isEmpty()) {
                val flatSnapshot = db.collection(COLLECTION_COMMITTEE_MEMBERS).get().await()
                results = flatSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CommitteeMember.fromFirestoreDoc(doc.id, data)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            results
        } catch (e: Exception) {
            handleFirestoreError("Fetching committee members", e)
            emptyList()
        }
    }

    // =========================================================================
    // 9. SUBSCRIPTIONS & MASTER ADMIN CONFIG
    // =========================================================================

    suspend fun saveUserSubscription(subscription: UserSubscription): Boolean {
        val db = firestore ?: return false
        return try {
            ensureAuthenticated()
            val docId = if (subscription.userId.isNotBlank()) subscription.userId else subscription.societyId.ifBlank { "sub_default" }
            val data = hashMapOf<String, Any?>(
                "userId" to subscription.userId,
                "userEmail" to subscription.userEmail,
                "societyId" to subscription.societyId,
                "isPro" to subscription.isPro,
                "planType" to subscription.planType,
                "status" to subscription.status,
                "trialStartTime" to subscription.trialStartTime,
                "startDate" to subscription.startDate,
                "expiryDate" to subscription.expiryDate,
                "transactionId" to subscription.transactionId,
                "amount" to subscription.amount,
                "paymentGateway" to subscription.paymentGateway,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_SUBSCRIPTIONS).document(docId).set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            handleFirestoreError("Saving subscription", e)
            false
        }
    }

    fun getUserSubscriptionFlow(userId: String, societyId: String): Flow<UserSubscription?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docId = userId.ifBlank { societyId.ifBlank { "sub_default" } }
        val listenerRegistration = db.collection(COLLECTION_SUBSCRIPTIONS)
            .document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Notice: Subscription listen error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val sub = UserSubscription(
                            userId = snapshot.getString("userId") ?: "",
                            userEmail = snapshot.getString("userEmail") ?: "",
                            societyId = snapshot.getString("societyId") ?: "",
                            isPro = snapshot.getBoolean("isPro") ?: false,
                            planType = snapshot.getString("planType") ?: "PRO_ANNUAL",
                            status = snapshot.getString("status") ?: "ACTIVE",
                            trialStartTime = snapshot.getLong("trialStartTime") ?: 0L,
                            startDate = snapshot.getLong("startDate") ?: 0L,
                            expiryDate = snapshot.getLong("expiryDate") ?: 0L,
                            transactionId = snapshot.getString("transactionId") ?: "",
                            amount = snapshot.getDouble("amount") ?: 499.0,
                            paymentGateway = snapshot.getString("paymentGateway") ?: "Razorpay",
                            lastUpdated = snapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                        )
                        trySend(sub)
                    } catch (e: Exception) {
                        Log.w(TAG, "Notice: parsing UserSubscription doc: ${e.message}")
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getUserSubscription(userId: String, societyId: String): UserSubscription? {
        val db = firestore ?: return null
        if (isCloudAccessRestricted && auth?.currentUser == null) return null
        val docId = userId.ifBlank { societyId.ifBlank { "sub_default" } }
        return try {
            ensureAuthenticated()
            val doc = db.collection(COLLECTION_SUBSCRIPTIONS).document(docId).get().await()
            if (doc.exists()) {
                UserSubscription(
                    userId = doc.getString("userId") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    societyId = doc.getString("societyId") ?: "",
                    isPro = doc.getBoolean("isPro") ?: false,
                    planType = doc.getString("planType") ?: "PRO_ANNUAL",
                    status = doc.getString("status") ?: "ACTIVE",
                    trialStartTime = doc.getLong("trialStartTime") ?: 0L,
                    startDate = doc.getLong("startDate") ?: 0L,
                    expiryDate = doc.getLong("expiryDate") ?: 0L,
                    transactionId = doc.getString("transactionId") ?: "",
                    amount = doc.getDouble("amount") ?: 499.0,
                    paymentGateway = doc.getString("paymentGateway") ?: "Razorpay",
                    lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            handleFirestoreError("Fetching subscription", e)
            null
        }
    }

    suspend fun getMasterAdminConfig(): Map<String, Any>? {
        val db = firestore ?: return null
        return try {
            val doc = db.collection(COLLECTION_APP_CONFIG).document("master_admin").get().await()
            if (doc.exists()) {
                doc.data
            } else {
                null
            }
        } catch (e: Exception) {
            handleFirestoreError("Fetching master admin config", e)
            null
        }
    }

    suspend fun saveMasterAdminConfig(masterPin: String, authorizedEmails: List<String>): Boolean {
        val db = firestore ?: return false
        return try {
            val data = mapOf(
                "masterPin" to masterPin.trim(),
                "authorizedEmails" to authorizedEmails,
                "isPinConfigured" to true,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            db.collection(COLLECTION_APP_CONFIG).document("master_admin")
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            handleFirestoreError("Saving master admin config", e)
            false
        }
    }

    fun observeMasterAdminConfig(onConfigChanged: (Map<String, Any>?) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection(COLLECTION_APP_CONFIG).document("master_admin")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        onConfigChanged(snapshot.data)
                    }
                }
        } catch (e: Exception) {
            null
        }
    }

    fun getFirebaseUserEmail(): String? {
        return auth?.currentUser?.email?.trim()?.lowercase()
    }

    // =========================================================================
    // Document Mapping Helpers
    // =========================================================================

    private fun mapDocToExpense(doc: DocumentSnapshot): Expense? {
        return try {
            Expense(
                id = doc.getLong("localId") ?: 0L,
                title = doc.getString("title") ?: "",
                category = doc.getString("category") ?: "Miscellaneous",
                amount = doc.getDouble("amount") ?: 0.0,
                expenseDate = doc.getLong("expenseDate") ?: 0L,
                paidTo = doc.getString("paidTo") ?: "",
                paymentMode = doc.getString("paymentMode") ?: "Cash",
                receiptUrl = doc.getString("receiptUrl") ?: "",
                paidByCommitteeMemberId = doc.getLong("paidByCommitteeMemberId") ?: 0L,
                recordedById = doc.getLong("recordedById") ?: 1L,
                recordedByName = doc.getString("recordedByName") ?: "",
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
                deletedByName = doc.getString("deletedByName") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun mapDocToDonation(doc: DocumentSnapshot): Donation? {
        return try {
            Donation(
                id = doc.getLong("localId") ?: 0L,
                donorName = doc.getString("donorName") ?: "",
                phone = doc.getString("phone") ?: "",
                amount = doc.getDouble("amount") ?: 0.0,
                status = doc.getString("status") ?: "received",
                paymentMode = doc.getString("paymentMode") ?: "Cash",
                paymentDate = doc.getLong("paymentDate") ?: 0L,
                receiptNo = doc.getString("receiptNo") ?: "",
                notes = doc.getString("notes") ?: "",
                recordedById = doc.getLong("recordedById") ?: 1L,
                recordedByName = doc.getString("recordedByName") ?: "",
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
                deletedByName = doc.getString("deletedByName") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun mapDocToMember(doc: DocumentSnapshot): Member? {
        return try {
            Member(
                id = doc.getLong("id") ?: 0L,
                name = doc.getString("name") ?: "",
                role = doc.getString("role") ?: "Executive Member",
                phone = doc.getString("phone") ?: "",
                email = doc.getString("email") ?: "",
                pin = doc.getString("pin") ?: "1234",
                isSuperAdmin = doc.getBoolean("isSuperAdmin") ?: false,
                flatNo = doc.getString("flatNo") ?: "A-101",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun mapDocToAuditLog(doc: DocumentSnapshot): AuditLog? {
        return try {
            AuditLog(
                id = (doc.getLong("localId") ?: 0L),
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
            null
        }
    }

    /**
     * Explicitly initializes the complete Firestore schema with nested collections under the society document:
     *
     * Hierarchy created:
     * - Parent Society Document: /societies/{societyName}
     * - Nested subcollections:
     *   - /societies/{societyName}/members/{memberId}
     *   - /societies/{societyName}/donations/{donationId}
     *   - /societies/{societyName}/expenses/{expenseId}
     *   - /societies/{societyName}/transactions/{transactionId}
     *   - /societies/{societyName}/pledges/{pledgeId}
     *   - /societies/{societyName}/committee_members/{memberId}
     *   - /societies/{societyName}/audit_logs/{logId}
     *
     * Creates the parent society document and seeds nested subcollection records
     * so that the full hierarchy is immediately present and navigable in the Firebase Console.
     */
    suspend fun initializeNestedFirestoreSchema(
        society: SocietyInfo,
        initialMembers: List<Member> = emptyList(),
        initialDonations: List<Donation> = emptyList(),
        initialExpenses: List<Expense> = emptyList(),
        initialTransactions: List<Transaction> = emptyList(),
        initialPledges: List<Pledge> = emptyList(),
        initialCommitteeMembers: List<CommitteeMember> = emptyList(),
        initialAuditLogs: List<AuditLog> = emptyList()
    ): Pair<Boolean, String> {
        val db = firestore ?: return Pair(false, "Firestore instance not available")
        clearLastError()
        resetCloudAccessStatus()

        try {
            ensureAuthenticated()
            val societyDocId = sanitizeSocietyDocId(society.name)
            activeSocietyName = society.name

            // 1. Initialize Society Master Document (/societies/{societyName})
            val societyRef = db.collection(COLLECTION_SOCIETIES).document(societyDocId)
            val societyData = hashMapOf<String, Any?>(
                "id" to societyDocId,
                "name" to society.name,
                "registrationNo" to society.registrationNo,
                "upiId" to society.upiId,
                "bankName" to society.bankName,
                "accountNumber" to society.accountNumber,
                "ifscCode" to society.ifscCode,
                "branchName" to society.branchName,
                "bankAccount" to society.bankAccount,
                "address" to society.address,
                "city" to society.city,
                "state" to society.state,
                "pinCode" to society.pinCode,
                "contactEmail" to society.contactEmail,
                "adminEmail" to society.adminEmail,
                "contactPhone" to society.contactPhone,
                "financialYear" to society.financialYear,
                "openingBalance" to society.openingBalance,
                "presidentName" to society.presidentName,
                "secretaryName" to society.secretaryName,
                "treasurerName" to society.treasurerName,
                "securityGatePhone" to society.securityGatePhone,
                "maintenanceLeadPhone" to society.maintenanceLeadPhone,
                "plumberPhone" to society.plumberPhone,
                "emergencyHelplinePhone" to society.emergencyHelplinePhone,
                "nestedCollections" to FieldValue.delete(),
                "schemaVersion" to FieldValue.delete(),
                "hierarchyPath" to FieldValue.delete(),
                "initializedAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            societyRef.set(societyData, SetOptions.merge()).await()

            // 2. Subcollection: members (/societies/{societyName}/members) - All donated members
            var memberCount = 0
            initialMembers.forEach { member ->
                val saved = saveMember(member, societyName = society.name)
                if (saved) memberCount++
            }

            // 3. Subcollection: committee_members (/societies/{societyName}/committee_members)
            var committeeCount = 0
            initialCommitteeMembers.forEach { cm ->
                val id = saveCommitteeMember(cm, societyName = society.name)
                if (!id.isNullOrBlank()) committeeCount++
            }

            // 4. Subcollection: donations (/societies/{societyName}/donations)
            var donationCount = 0
            initialDonations.forEach { donation ->
                val id = saveDonation(donation, societyName = society.name)
                if (!id.isNullOrBlank()) donationCount++
            }

            // 5. Subcollection: expenses (/societies/{societyName}/expenses)
            var expenseCount = 0
            initialExpenses.forEach { expense ->
                val id = saveExpense(expense, societyName = society.name)
                if (!id.isNullOrBlank()) expenseCount++
            }

            // 6. Subcollection: transactions (/societies/{societyName}/transactions)
            var transactionCount = 0
            initialTransactions.forEach { txn ->
                val id = saveTransaction(txn, societyName = society.name)
                if (!id.isNullOrBlank()) transactionCount++
            }

            // 7. Subcollection: pledges (/societies/{societyName}/pledges)
            var pledgeCount = 0
            initialPledges.forEach { pledge ->
                val id = savePledge(pledge, societyName = society.name)
                if (!id.isNullOrBlank()) pledgeCount++
            }

            // 8. Subcollection: audit_logs (/societies/{societyName}/audit_logs)
            var auditLogCount = 0
            initialAuditLogs.forEach { log ->
                val id = saveAuditLog(log, societyName = society.name)
                if (!id.isNullOrBlank()) auditLogCount++
            }

            val summary = "Cloud schema initialized for '$societyDocId': " +
                "Society Details saved with respective records: " +
                "$memberCount members, $committeeCount committee members, $donationCount donations, " +
                "$expenseCount expenses, $transactionCount transactions, $pledgeCount pledges, and $auditLogCount audit logs."
            Log.d(TAG, summary)
            return Pair(true, summary)
        } catch (e: Exception) {
            handleFirestoreError("Initializing Schema", e)
            val err = lastCloudError ?: e.localizedMessage ?: "Failed to initialize cloud schema"
            Log.e(TAG, "Error initializing nested schema: $err", e)
            return Pair(false, err)
        }
    }

    /**
     * Completely recreates all collections and subcollections in Firestore with the new schema:
     * Hierarchy: /societies/{societyName}/[members, donations, expenses, transactions, pledges, committee_members, audit_logs]
     */
    suspend fun recreateAllDataWithNewStructure(
        society: SocietyInfo,
        members: List<Member> = emptyList(),
        donations: List<Donation> = emptyList(),
        expenses: List<Expense> = emptyList(),
        transactions: List<Transaction> = emptyList(),
        pledges: List<Pledge> = emptyList(),
        committeeMembers: List<CommitteeMember> = emptyList(),
        auditLogs: List<AuditLog> = emptyList()
    ): Pair<Boolean, String> {
        return initializeNestedFirestoreSchema(
            society = society,
            initialMembers = members,
            initialDonations = donations,
            initialExpenses = expenses,
            initialTransactions = transactions,
            initialPledges = pledges,
            initialCommitteeMembers = committeeMembers,
            initialAuditLogs = auditLogs
        )
    }

    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_APP_CONFIG = "app_config"
        const val COLLECTION_EXPENSES = "expenses"
        const val COLLECTION_DONATIONS = "donations"
        const val COLLECTION_TRANSACTIONS = "transactions"
        const val COLLECTION_PLEDGES = "pledges"
        const val COLLECTION_COMMITTEE_MEMBERS = "committee_members"
        const val COLLECTION_SOCIETIES = "societies"
        const val COLLECTION_MEMBERS = "members"
        const val COLLECTION_AUDIT_LOGS = "audit_logs"
        const val COLLECTION_SUBSCRIPTIONS = "subscriptions"
    }
}
