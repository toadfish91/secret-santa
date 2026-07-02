package com.kevin.secretsanta.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exclusions",
    foreignKeys = [
        ForeignKey(entity = GroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ParticipantEntity::class, parentColumns = ["id"], childColumns = ["participantIdA"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ParticipantEntity::class, parentColumns = ["id"], childColumns = ["participantIdB"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("groupId"), Index("participantIdA"), Index("participantIdB")]
)
data class ExclusionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    // Two-way:  participantIdA and participantIdB are normalized (lower ID first) — order doesn't matter
    // One-way:  participantIdA CANNOT get participantIdB, but B can still get A — order matters
    val participantIdA: Long,
    val participantIdB: Long,
    val twoWay: Boolean = true
)
