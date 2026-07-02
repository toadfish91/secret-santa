package com.kevin.secretsanta.data

class SecretSantaRepository(private val db: SecretSantaDatabase) {

    val groups = db.groupDao().getAllGroups()

    suspend fun createGroup(
        title: String,
        year: Int,
        organizerName: String,
        budgetDollars: Int? = null,
        previousGroupId: Long? = null
    ): Long = db.groupDao().insert(
        GroupEntity(
            title = title,
            year = year,
            organizerName = organizerName,
            budgetDollars = budgetDollars,
            previousGroupId = previousGroupId
        )
    )

    fun getGroup(groupId: Long) = db.groupDao().getGroupById(groupId)
    suspend fun updateGroup(group: GroupEntity) = db.groupDao().update(group)
    suspend fun deleteGroup(group: GroupEntity) = db.groupDao().delete(group)

    fun getParticipants(groupId: Long) = db.participantDao().getParticipantsForGroup(groupId)
    suspend fun getParticipantsOnce(groupId: Long) = db.participantDao().getParticipantsOnce(groupId)
    suspend fun addParticipant(
        groupId: Long,
        firstName: String,
        lastName: String,
        phone: String,
        sourceParticipantId: Long? = null
    ): Long = db.participantDao().insert(
        ParticipantEntity(
            groupId = groupId,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phone,
            sourceParticipantId = sourceParticipantId
        )
    )
    suspend fun updateParticipant(participant: ParticipantEntity) = db.participantDao().update(participant)
    suspend fun deleteParticipant(participant: ParticipantEntity) = db.participantDao().delete(participant)
    suspend fun getParticipantById(id: Long) = db.participantDao().getParticipantById(id)

    fun getAssignments(groupId: Long) = db.assignmentDao().getAssignmentsForGroup(groupId)
    suspend fun getRawAssignmentPairs(groupId: Long) = db.assignmentDao().getRawPairsForGroup(groupId)
    suspend fun saveAssignments(groupId: Long, pairs: List<Pair<Long, Long>>) {
        db.assignmentDao().deleteForGroup(groupId)
        db.assignmentDao().insertAll(
            pairs.map { AssignmentEntity(groupId = groupId, giverId = it.first, receiverId = it.second) }
        )
    }

    // Exclusions
    fun getExclusions(groupId: Long) = db.exclusionDao().getExclusionsForGroup(groupId)
    suspend fun getExclusionsOnce(groupId: Long) = db.exclusionDao().getExclusionsOnce(groupId)
    suspend fun addExclusion(groupId: Long, idA: Long, idB: Long, twoWay: Boolean) {
        if (twoWay) {
            val lo = minOf(idA, idB)
            val hi = maxOf(idA, idB)
            if (db.exclusionDao().twoWayExclusionExists(groupId, lo, hi) == 0) {
                db.exclusionDao().insert(ExclusionEntity(groupId = groupId, participantIdA = lo, participantIdB = hi, twoWay = true))
            }
        } else {
            if (db.exclusionDao().oneWayExclusionExists(groupId, idA, idB) == 0) {
                db.exclusionDao().insert(ExclusionEntity(groupId = groupId, participantIdA = idA, participantIdB = idB, twoWay = false))
            }
        }
    }
    suspend fun deleteExclusion(id: Long) = db.exclusionDao().deleteById(id)

    /**
     * Duplicates [sourceGroupId] into a new group with the given details.
     * - All participants are copied with sourceParticipantId set so previous-year
     *   assignments can be excluded automatically during generation.
     * - All exclusions are copied with participant IDs remapped to the new group.
     * - Assignments are NOT copied (they belong to the previous year).
     */
    suspend fun duplicateGroup(
        sourceGroupId: Long,
        newTitle: String,
        newYear: Int,
        newOrganizerName: String,
        newBudgetDollars: Int?
    ): Long {
        // Create the new group, remembering where it came from
        val newGroupId = createGroup(
            title = newTitle,
            year = newYear,
            organizerName = newOrganizerName,
            budgetDollars = newBudgetDollars,
            previousGroupId = sourceGroupId
        )

        // Copy participants, recording their old IDs
        val oldParticipants = getParticipantsOnce(sourceGroupId)
        val idMapping = mutableMapOf<Long, Long>() // oldId → newId
        oldParticipants.forEach { old ->
            val newId = addParticipant(
                groupId = newGroupId,
                firstName = old.firstName,
                lastName = old.lastName,
                phone = old.phoneNumber,
                sourceParticipantId = old.id
            )
            idMapping[old.id] = newId
        }

        // Copy exclusions, remapping participant IDs to the new group
        val oldExclusions = getExclusionsOnce(sourceGroupId)
        oldExclusions.forEach { exc ->
            val newIdA = idMapping[exc.participantIdA]
            val newIdB = idMapping[exc.participantIdB]
            if (newIdA != null && newIdB != null) {
                db.exclusionDao().insert(
                    ExclusionEntity(
                        groupId = newGroupId,
                        participantIdA = newIdA,
                        participantIdB = newIdB,
                        twoWay = exc.twoWay
                    )
                )
            }
        }

        return newGroupId
    }
}
