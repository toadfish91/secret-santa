package com.kevin.secretsanta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val year: Int,
    val organizerName: String,
    // Whole dollar amount; null means no budget was set
    val budgetDollars: Int? = null,
    // Set when this group was duplicated from another; used to exclude previous year's assignments
    val previousGroupId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
