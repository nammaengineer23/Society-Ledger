package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MaintenanceCharge
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceChargeDao {
    @Query("SELECT * FROM maintenance_charges ORDER BY billingPeriod DESC, flatNo ASC")
    fun observeAll(): Flow<List<MaintenanceCharge>>

    @Query("SELECT * FROM maintenance_charges WHERE flatNo = :flatNo ORDER BY billingPeriod DESC")
    fun observeForFlat(flatNo: String): Flow<List<MaintenanceCharge>>

    @Query("SELECT * FROM maintenance_charges WHERE billingPeriod = :period ORDER BY flatNo ASC")
    fun observeForPeriod(period: String): Flow<List<MaintenanceCharge>>

    @Query("SELECT * FROM maintenance_charges WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MaintenanceCharge?

    @Query("SELECT * FROM maintenance_charges WHERE flatNo = :flatNo AND billingPeriod = :period LIMIT 1")
    suspend fun findForFlatAndPeriod(flatNo: String, period: String): MaintenanceCharge?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(charge: MaintenanceCharge): Long

    @Update
    suspend fun update(charge: MaintenanceCharge)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM maintenance_charges WHERE billingPeriod = :period AND status != 'WAIVED'")
    suspend fun totalAssessedForPeriod(period: String): Double

    @Query("SELECT COALESCE(SUM(MAX(amount - amountPaid, 0)), 0) FROM maintenance_charges WHERE status IN ('OPEN', 'PARTIALLY_PAID')")
    suspend fun totalOutstanding(): Double
}
