package com.kevin.secretsanta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExclusionDao {

    @Insert
    suspend fun insert(exclusion: ExclusionEntity)

    @Query("DELETE FROM exclusions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM exclusions WHERE groupId = :groupId")
    fun getExclusionsForGroup(groupId: Long): Flow<List<ExclusionEntity>>

    @Query("SELECT * FROM exclusions WHERE groupId = :groupId")
    suspend fun getExclusionsOnce(groupId: Long): List<ExclusionEntity>

    /**
     * Duplicate check for two-way exclusions: (A,B) and (B,A) are the same thing
     * since they're stored normalized (lower ID first).
     */
    @Query("""
        SELECT COUNT(*) FROM exclusions
        WHERE groupId = :groupId
          AND twoWay = 1
          AND participantIdA = :idA
          AND participantIdB = :idB
    """)
    suspend fun twoWayExclusionExists(groupId: Long, idA: Long, idB: Long): Int

    /**
     * Duplicate check for one-way exclusions: (A→B) and (B→A) are different,
     * but the exact same direction shouldn't be added twice.
     */
    @Query("""
        SELECT COUNT(*) FROM exclusions
        WHERE groupId = :groupId
          AND twoWay = 0
          AND participantIdA = :idA
          AND participantIdB = :idB
    """)
    suspend fun oneWayExclusionExists(groupId: Long, idA: Long, idB: Long): Int
}
