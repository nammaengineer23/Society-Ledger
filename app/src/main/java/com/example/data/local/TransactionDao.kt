package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for unified Transaction ledger records in Room.
 */
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY paymentDate DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY paymentDate DESC")
    suspend fun getAllTransactionsList(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE committeeMemberId = :memberId AND isDeleted = 0 ORDER BY paymentDate ASC, id ASC")
    fun getMemberLedger(memberId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE committeeMemberId = :memberId AND isDeleted = 0 ORDER BY paymentDate ASC, id ASC")
    suspend fun getMemberLedgerList(memberId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE transactionType = :type AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun getTransactionsByType(type: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = :status AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun getTransactionsByStatus(status: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getTransactionByFirestoreId(firestoreId: String): Transaction?

    @Query("SELECT * FROM transactions WHERE referenceNo = :ref LIMIT 1")
    suspend fun getTransactionByReferenceNo(ref: String): Transaction?

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    suspend fun getTransactionCount(): Int

    @Query("SELECT SUM(amount) FROM transactions WHERE transactionType = 'INCOME' AND isDeleted = 0 AND status = 'COMPLETED'")
    fun getTotalIncomeFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE transactionType = 'EXPENSE' AND isDeleted = 0")
    fun getTotalExpenseFlow(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
