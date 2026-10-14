package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Donation
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations ORDER BY paymentDate DESC")
    fun getAllDonations(): Flow<List<Donation>>

    @Query("SELECT * FROM donations ORDER BY paymentDate DESC")
    suspend fun getAllDonationsList(): List<Donation>

    @Query("SELECT * FROM donations WHERE status = 'pending' ORDER BY paymentDate DESC")
    fun getPendingDonations(): Flow<List<Donation>>

    @Query("SELECT * FROM donations WHERE status = 'received' ORDER BY paymentDate DESC")
    fun getReceivedDonations(): Flow<List<Donation>>

    @Query("SELECT * FROM donations WHERE id = :id LIMIT 1")
    suspend fun getDonationById(id: Long): Donation?

    @Query("SELECT * FROM donations WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getDonationByFirestoreId(firestoreId: String): Donation?

    @Query("SELECT * FROM donations WHERE receiptNo = :receiptNo LIMIT 1")
    suspend fun getDonationByReceiptNo(receiptNo: String): Donation?

    @Query("SELECT * FROM donations WHERE phone = :phone ORDER BY paymentDate DESC")
    fun getDonationsByPhone(phone: String): Flow<List<Donation>>

    @Query("SELECT COUNT(*) FROM donations")
    suspend fun getDonationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: Donation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(donations: List<Donation>)

    @Update
    suspend fun updateDonation(donation: Donation)

    @Delete
    suspend fun deleteDonation(donation: Donation)

    @Query("DELETE FROM donations WHERE id = :id")
    suspend fun deleteDonationById(id: Long)

    @Query("DELETE FROM donations")
    suspend fun deleteAllDonations()

    @Query("SELECT SUM(amount) FROM donations WHERE status = 'received' AND isDeleted = 0")
    fun getTotalReceivedFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM donations WHERE status = 'pending' AND isDeleted = 0")
    fun getTotalPendingFlow(): Flow<Double?>
}
