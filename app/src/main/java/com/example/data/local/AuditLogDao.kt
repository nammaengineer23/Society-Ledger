package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    suspend fun getAllAuditLogsList(): List<AuditLog>

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun getAuditLogCount(): Int

    @Query("SELECT * FROM audit_logs WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getAuditLogByFirestoreId(firestoreId: String): AuditLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AuditLog>)

    @androidx.room.Update
    suspend fun updateAuditLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllLogs()
}
