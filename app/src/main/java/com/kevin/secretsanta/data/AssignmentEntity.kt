package com.kevin.secretsanta.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(entity = GroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ParticipantEntity::class, parentColumns = ["id"], childColumns = ["giverId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ParticipantEntity::class, parentColumns = ["id"], childColumns = ["receiverId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("groupId"), Index("giverId"), Index("receiverId")]
)
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val giverId: Long,
    val receiverId: Long
)
