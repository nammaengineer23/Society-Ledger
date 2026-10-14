package com.example.data.repository

import android.util.Log
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository interface specifically designed to manage Society Donations
 * and Expenses in Firebase Firestore.
 */
interface SocietyFinanceRepositoryContract {
    // ==========================================
    // DONATION OPERATIONS
    // ==========================================
    suspend fun saveDonation(donation: Donation): Result<String>
    suspend fun getDonationById(donationId: String): Result<Donation?>
    suspend fun getDonationsForSociety(societyId: String = ""): Result<List<Donation>>
    fun getDonationsFlow(societyId: String = ""): Flow<List<Donation>>
    suspend fun getDonationsByDonorPhone(phone: String): Result<List<Donation>>
    suspend fun updateDonation(donationId: String, donation: Donation): Result<Unit>
    suspend fun updateDonationStatus(donationId: String, newStatus: String): Result<Unit>
    suspend fun deleteDonation(donationId: String): Result<Unit>
    suspend fun uploadAllDonations(donations: List<Donation>): Result<Int>

    // ==========================================
    // EXPENSE OPERATIONS
    // ==========================================
    suspend fun saveExpense(expense: Expense): Result<String>
    suspend fun getExpenseById(expenseId: String): Result<Expense?>
    suspend fun getExpensesForSociety(societyId: String = ""): Result<List<Expense>>
    fun getExpensesFlow(societyId: String = ""): Flow<List<Expense>>
    suspend fun getExpensesByCategory(category: String, societyId: String = ""): Result<List<Expense>>
    suspend fun updateExpense(expenseId: String, expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
    suspend fun uploadAllExpenses(expenses: List<Expense>): Result<Int>

    // ==========================================
    // COMBINED / SUMMARY OPERATIONS
    // ==========================================
    suspend fun getFinancialSummary(societyId: String = ""): Result<SocietyFinancialSummary>
}

/**
 * Model representing aggregated financial totals for a Society.
 */
data class SocietyFinancialSummary(
    val totalDonationsReceived: Double = 0.0,
    val totalDonationsPending: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val donationCount: Int = 0,
    val expenseCount: Int = 0
)

/**
 * Production-ready Firestore Repository implementation for managing
 * society donations and expenses in Firebase Firestore.
 */
class SocietyFinanceRepository(
    private val firestoreInstance: FirebaseFirestore? = null,
    private val authInstance: FirebaseAuth? = null
) : SocietyFinanceRepositoryContract {

    private val db: FirebaseFirestore? by lazy {
        firestoreInstance ?: try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization notice: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        authInstance ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth initialization notice: ${e.message}")
            null
        }
    }

    private fun logNotice(action: String, e: Throwable) {
        if (e is com.google.firebase.firestore.FirebaseFirestoreException &&
            e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
        ) {
            Log.w(TAG, "Notice: $action - Cloud Firestore permissions restricted. Operating in local mode.")
        } else {
            Log.w(TAG, "Notice: $action - ${e.message ?: "network/storage unavailable"}")
        }
    }

    // =========================================================================
    // DONATION OPERATIONS
    // =========================================================================

    override suspend fun saveDonation(donation: Donation): Result<String> {
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

            val updatedDonation = donation.copy(
                firestoreId = firestoreId,
                userId = currentUid,
                userEmail = currentEmail
            )

            docRef.set(updatedDonation.toFirestoreMap(), SetOptions.merge()).await()
            Log.d(TAG, "Donation saved to Firestore with ID: $firestoreId")
            Result.success(firestoreId)
        } catch (e: Exception) {
            logNotice("Saving donation", e)
            Result.failure(e)
        }
    }

    override suspend fun getDonationById(donationId: String): Result<Donation?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val doc = firestore.collection(COLLECTION_DONATIONS).document(donationId).get().await()
            if (doc.exists()) {
                Result.success(Donation.fromFirestore(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logNotice("Fetching donation $donationId", e)
            Result.failure(e)
        }
    }

    override suspend fun getDonationsForSociety(societyId: String): Result<List<Donation>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val query = if (societyId.isNotBlank()) {
                firestore.collection(COLLECTION_DONATIONS)
                    .whereEqualTo("societyId", societyId)
            } else {
                firestore.collection(COLLECTION_DONATIONS)
            }

            val querySnapshot = query.get().await()
            val donations = querySnapshot.documents
                .mapNotNull { Donation.fromFirestore(it) }
                .sortedByDescending { it.paymentDate }

            Result.success(donations)
        } catch (e: Exception) {
            logNotice("Getting donations for society $societyId", e)
            Result.failure(e)
        }
    }

    override fun getDonationsFlow(societyId: String): Flow<List<Donation>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = if (societyId.isNotBlank()) {
            firestore.collection(COLLECTION_DONATIONS).whereEqualTo("societyId", societyId)
        } else {
            firestore.collection(COLLECTION_DONATIONS)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logNotice("Listening to donations", error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { Donation.fromFirestore(it) }
                ?.sortedByDescending { it.paymentDate } ?: emptyList()
            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getDonationsByDonorPhone(phone: String): Result<List<Donation>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = firestore.collection(COLLECTION_DONATIONS)
                .whereEqualTo("phone", phone.trim())
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { Donation.fromFirestore(it) }
            Result.success(list)
        } catch (e: Exception) {
            logNotice("Fetching donations by phone $phone", e)
            Result.failure(e)
        }
    }

    override suspend fun updateDonation(donationId: String, donation: Donation): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = donation.toFirestoreMap()
            data["updatedAt"] = System.currentTimeMillis()

            firestore.collection(COLLECTION_DONATIONS).document(donationId)
                .update(data as Map<String, Any?>)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating donation $donationId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateDonationStatus(donationId: String, newStatus: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_DONATIONS).document(donationId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating donation status $donationId", e)
            Result.failure(e)
        }
    }

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

    override suspend fun uploadAllDonations(donations: List<Donation>): Result<Int> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (donations.isEmpty()) return Result.success(0)

        return try {
            var uploadedCount = 0
            val chunks = donations.chunked(450)
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (donation in chunk) {
                    val docRef = if (donation.firestoreId.isNotBlank()) {
                        firestore.collection(COLLECTION_DONATIONS).document(donation.firestoreId)
                    } else if (donation.id > 0) {
                        firestore.collection(COLLECTION_DONATIONS).document("don_${donation.id}")
                    } else {
                        firestore.collection(COLLECTION_DONATIONS).document()
                    }
                    val data = donation.toFirestoreMap()
                    data["firestoreId"] = docRef.id
                    batch.set(docRef, data, SetOptions.merge())
                    uploadedCount++
                }
                batch.commit().await()
            }
            Log.d(TAG, "Uploaded $uploadedCount donations to Firestore")
            Result.success(uploadedCount)
        } catch (e: Exception) {
            logNotice("Batch uploading donations", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // EXPENSE OPERATIONS
    // =========================================================================

    override suspend fun saveExpense(expense: Expense): Result<String> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val docRef = if (expense.firestoreId.isNotBlank()) {
                firestore.collection(COLLECTION_EXPENSES).document(expense.firestoreId)
            } else {
                firestore.collection(COLLECTION_EXPENSES).document()
            }
            val firestoreId = docRef.id

            val currentUser = auth?.currentUser
            val currentUid = currentUser?.uid ?: expense.userId
            val currentEmail = currentUser?.email ?: expense.userEmail

            val updatedExpense = expense.copy(
                firestoreId = firestoreId,
                userId = currentUid,
                userEmail = currentEmail
            )

            docRef.set(updatedExpense.toFirestoreMap(), SetOptions.merge()).await()
            Log.d(TAG, "Expense saved to Firestore with ID: $firestoreId")
            Result.success(firestoreId)
        } catch (e: Exception) {
            logNotice("Saving expense", e)
            Result.failure(e)
        }
    }

    override suspend fun getExpenseById(expenseId: String): Result<Expense?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val doc = firestore.collection(COLLECTION_EXPENSES).document(expenseId).get().await()
            if (doc.exists()) {
                Result.success(Expense.fromFirestore(doc))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logNotice("Fetching expense $expenseId", e)
            Result.failure(e)
        }
    }

    override suspend fun getExpensesForSociety(societyId: String): Result<List<Expense>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val query = if (societyId.isNotBlank()) {
                firestore.collection(COLLECTION_EXPENSES).whereEqualTo("societyId", societyId)
            } else {
                firestore.collection(COLLECTION_EXPENSES)
            }

            val querySnapshot = query.get().await()
            val expenses = querySnapshot.documents
                .mapNotNull { Expense.fromFirestore(it) }
                .sortedByDescending { it.expenseDate }

            Result.success(expenses)
        } catch (e: Exception) {
            logNotice("Getting expenses for society $societyId", e)
            Result.failure(e)
        }
    }

    override fun getExpensesFlow(societyId: String): Flow<List<Expense>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = if (societyId.isNotBlank()) {
            firestore.collection(COLLECTION_EXPENSES).whereEqualTo("societyId", societyId)
        } else {
            firestore.collection(COLLECTION_EXPENSES)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logNotice("Listening to expenses", error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { Expense.fromFirestore(it) }
                ?.sortedByDescending { it.expenseDate } ?: emptyList()
            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getExpensesByCategory(category: String, societyId: String): Result<List<Expense>> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val baseQuery = if (societyId.isNotBlank()) {
                firestore.collection(COLLECTION_EXPENSES)
                    .whereEqualTo("societyId", societyId)
                    .whereEqualTo("category", category)
            } else {
                firestore.collection(COLLECTION_EXPENSES)
                    .whereEqualTo("category", category)
            }

            val snapshot = baseQuery.get().await()
            val list = snapshot.documents.mapNotNull { Expense.fromFirestore(it) }
            Result.success(list)
        } catch (e: Exception) {
            logNotice("Getting expenses by category $category", e)
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(expenseId: String, expense: Expense): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = expense.toFirestoreMap()
            data["updatedAt"] = System.currentTimeMillis()

            firestore.collection(COLLECTION_EXPENSES).document(expenseId)
                .update(data as Map<String, Any?>)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Updating expense $expenseId", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            firestore.collection(COLLECTION_EXPENSES).document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            logNotice("Deleting expense $expenseId", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadAllExpenses(expenses: List<Expense>): Result<Int> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (expenses.isEmpty()) return Result.success(0)

        return try {
            var uploadedCount = 0
            val chunks = expenses.chunked(450)
            for (chunk in chunks) {
                val batch = firestore.batch()
                for (expense in chunk) {
                    val docRef = if (expense.firestoreId.isNotBlank()) {
                        firestore.collection(COLLECTION_EXPENSES).document(expense.firestoreId)
                    } else if (expense.id > 0) {
                        firestore.collection(COLLECTION_EXPENSES).document("exp_${expense.id}")
                    } else {
                        firestore.collection(COLLECTION_EXPENSES).document()
                    }
                    val data = expense.toFirestoreMap()
                    data["firestoreId"] = docRef.id
                    batch.set(docRef, data, SetOptions.merge())
                    uploadedCount++
                }
                batch.commit().await()
            }
            Log.d(TAG, "Uploaded $uploadedCount expenses to Firestore")
            Result.success(uploadedCount)
        } catch (e: Exception) {
            logNotice("Batch uploading expenses", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // FINANCIAL SUMMARY
    // =========================================================================

    override suspend fun getFinancialSummary(societyId: String): Result<SocietyFinancialSummary> {
        val donationsResult = getDonationsForSociety(societyId)
        val expensesResult = getExpensesForSociety(societyId)

        if (donationsResult.isFailure && expensesResult.isFailure) {
            return Result.failure(
                donationsResult.exceptionOrNull()
                    ?: expensesResult.exceptionOrNull()
                    ?: Exception("Failed to retrieve financial summary")
            )
        }

        val donations = donationsResult.getOrDefault(emptyList()).filter { !it.isDeleted }
        val expenses = expensesResult.getOrDefault(emptyList()).filter { !it.isDeleted }

        var totalReceived = 0.0
        var totalPending = 0.0
        for (donation in donations) {
            if (donation.status.equals("received", ignoreCase = true) || donation.status.equals("COMPLETED", ignoreCase = true)) {
                totalReceived += donation.amount
            } else {
                totalPending += donation.amount
            }
        }

        val totalExp = expenses.sumOf { it.amount }
        val net = totalReceived - totalExp

        return Result.success(
            SocietyFinancialSummary(
                totalDonationsReceived = totalReceived,
                totalDonationsPending = totalPending,
                totalExpenses = totalExp,
                netBalance = net,
                donationCount = donations.size,
                expenseCount = expenses.size
            )
        )
    }

    companion object {
        private const val TAG = "SocietyFinanceRepo"
        const val COLLECTION_DONATIONS = "donations"
        const val COLLECTION_EXPENSES = "expenses"
    }
}
