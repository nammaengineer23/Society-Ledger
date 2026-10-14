package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommitteeMember
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Committee Members and office bearers in Room.
 */
@Dao
interface CommitteeMemberDao {
    @Query("SELECT * FROM committee_members ORDER BY id ASC")
    fun getAllCommitteeMembers(): Flow<List<CommitteeMember>>

    @Query("SELECT * FROM committee_members ORDER BY id ASC")
    suspend fun getAllCommitteeMembersList(): List<CommitteeMember>

    @Query("SELECT * FROM committee_members WHERE status = 'ACTIVE' ORDER BY id ASC")
    fun getActiveCommitteeMembers(): Flow<List<CommitteeMember>>

    @Query("SELECT * FROM committee_members WHERE id = :id LIMIT 1")
    suspend fun getCommitteeMemberById(id: Long): CommitteeMember?

    @Query("SELECT * FROM committee_members WHERE role = :role LIMIT 1")
    suspend fun getCommitteeMemberByRole(role: String): CommitteeMember?

    @Query("SELECT COUNT(*) FROM committee_members")
    suspend fun getCommitteeMemberCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitteeMember(member: CommitteeMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<CommitteeMember>)

    @Update
    suspend fun updateCommitteeMember(member: CommitteeMember)

    @Delete
    suspend fun deleteCommitteeMember(member: CommitteeMember)

    @Query("DELETE FROM committee_members WHERE id = :id")
    suspend fun deleteCommitteeMemberById(id: Long)

    @Query("DELETE FROM committee_members")
    suspend fun deleteAllCommitteeMembers()
}
