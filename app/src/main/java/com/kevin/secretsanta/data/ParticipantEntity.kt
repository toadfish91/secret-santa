package com.kevin.secretsanta.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "participants",
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupId")]
)
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    // Set when this participant was copied from another group during duplication.
    // Used to map new IDs back to the previous year's participant IDs when
    // looking up last year's assignments to exclude them.
    val sourceParticipantId: Long? = null
)
