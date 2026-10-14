package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Pledge
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Pledges and committed funds in Room.
 */
@Dao
interface PledgeDao {
    @Query("SELECT * FROM pledges ORDER BY pledgeDate DESC")
    fun getAllPledges(): Flow<List<Pledge>>

    @Query("SELECT * FROM pledges ORDER BY pledgeDate DESC")
    suspend fun getAllPledgesList(): List<Pledge>

    @Query("SELECT * FROM pledges WHERE status = 'PENDING' ORDER BY pledgeDate DESC")
    fun getPendingPledges(): Flow<List<Pledge>>

    @Query("SELECT * FROM pledges WHERE status = 'FULFILLED' ORDER BY pledgeDate DESC")
    fun getFulfilledPledges(): Flow<List<Pledge>>

    @Query("SELECT * FROM pledges WHERE id = :id LIMIT 1")
    suspend fun getPledgeById(id: Long): Pledge?

    @Query("SELECT * FROM pledges WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getPledgeByFirestoreId(firestoreId: String): Pledge?

    @Query("SELECT * FROM pledges WHERE phone = :phone ORDER BY pledgeDate DESC")
    fun getPledgesByPhone(phone: String): Flow<List<Pledge>>

    @Query("SELECT COUNT(*) FROM pledges")
    suspend fun getPledgeCount(): Int

    @Query("SELECT COUNT(*) FROM pledges WHERE status = 'PENDING'")
    fun getPendingPledgeCountFlow(): Flow<Int>

    @Query("SELECT SUM(amount) FROM pledges")
    fun getTotalPledgedFlow(): Flow<Double?>

    @Query("SELECT SUM(amount - fulfilledAmount) FROM pledges WHERE status = 'PENDING'")
    fun getTotalPendingPledgeAmountFlow(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPledge(pledge: Pledge): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pledges: List<Pledge>)

    @Update
    suspend fun updatePledge(pledge: Pledge)

    @Delete
    suspend fun deletePledge(pledge: Pledge)

    @Query("DELETE FROM pledges WHERE id = :id")
    suspend fun deletePledgeById(id: Long)

    @Query("DELETE FROM pledges")
    suspend fun deleteAllPledges()
}
