package com.kevin.secretsanta.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Insert
    suspend fun insert(participant: ParticipantEntity): Long

    @Update
    suspend fun update(participant: ParticipantEntity)

    @Delete
    suspend fun delete(participant: ParticipantEntity)

    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY firstName")
    fun getParticipantsForGroup(groupId: Long): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE groupId = :groupId ORDER BY firstName")
    suspend fun getParticipantsOnce(groupId: Long): List<ParticipantEntity>

    @Query("SELECT * FROM participants WHERE id = :id")
    suspend fun getParticipantById(id: Long): ParticipantEntity?
}
