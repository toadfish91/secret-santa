package com.kevin.secretsanta.data

data class AssignmentWithNames(
    val assignmentId: Long,
    val giverId: Long,
    val giverFirstName: String,
    val giverLastName: String,
    val giverPhone: String,
    val receiverId: Long,
    val receiverFirstName: String,
    val receiverLastName: String
)
