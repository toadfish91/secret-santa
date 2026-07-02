package com.kevin.secretsanta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Insert
    suspend fun insertAll(assignments: List<AssignmentEntity>)

    @Query("DELETE FROM assignments WHERE groupId = :groupId")
    suspend fun deleteForGroup(groupId: Long)

    @Query("""
        SELECT
            a.id AS assignmentId,
            g.id AS giverId, g.firstName AS giverFirstName, g.lastName AS giverLastName, g.phoneNumber AS giverPhone,
            r.id AS receiverId, r.firstName AS receiverFirstName, r.lastName AS receiverLastName
        FROM assignments a
        JOIN participants g ON a.giverId = g.id
        JOIN participants r ON a.receiverId = r.id
        WHERE a.groupId = :groupId
        ORDER BY g.firstName
    """)
    fun getAssignmentsForGroup(groupId: Long): Flow<List<AssignmentWithNames>>

    // Raw (giverId, receiverId) pairs — used when excluding previous year's assignments
    @Query("SELECT giverId, receiverId FROM assignments WHERE groupId = :groupId")
    suspend fun getRawPairsForGroup(groupId: Long): List<RawAssignmentPair>
}

data class RawAssignmentPair(val giverId: Long, val receiverId: Long)
