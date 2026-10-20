package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Donor
import kotlinx.coroutines.flow.Flow

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors WHERE societyId = :societyId ORDER BY name COLLATE NOCASE ASC")
    fun observeForSociety(societyId: String): Flow<List<Donor>>

    @Query("SELECT * FROM donors WHERE societyId = :societyId ORDER BY name COLLATE NOCASE ASC")
    suspend fun getForSociety(societyId: String): List<Donor>

    @Query("SELECT * FROM donors WHERE societyId = :societyId AND (lower(name) = lower(:name) OR (:phone != '' AND phone = :phone)) LIMIT 1")
    suspend fun findExisting(societyId: String, name: String, phone: String): Donor?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(donor: Donor): Long

    @Query("UPDATE donors SET phone = :phone, email = :email, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateContact(id: Long, phone: String, email: String, updatedAt: Long)
}
