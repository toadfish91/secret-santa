package com.kevin.secretsanta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.secretsanta.data.AssignmentWithNames
import com.kevin.secretsanta.data.GroupEntity
import com.kevin.secretsanta.data.ParticipantEntity
import com.kevin.secretsanta.data.SecretSantaRepository
import com.kevin.secretsanta.util.DerangementUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupDetailViewModel(private val repository: SecretSantaRepository) : ViewModel() {

    private var groupId: Long = -1
    private var initialized = false

    private val _group = MutableStateFlow<GroupEntity?>(null)
    val group: StateFlow<GroupEntity?> = _group

    private val _participants = MutableStateFlow<List<ParticipantEntity>>(emptyList())
    val participants: StateFlow<List<ParticipantEntity>> = _participants

    private val _assignments = MutableStateFlow<List<AssignmentWithNames>>(emptyList())
    val assignments: StateFlow<List<AssignmentWithNames>> = _assignments

    fun init(groupId: Long) {
        if (initialized && this.groupId == groupId) return
        initialized = true
        this.groupId = groupId
        viewModelScope.launch { repository.getGroup(groupId).collect { _group.value = it } }
        viewModelScope.launch { repository.getParticipants(groupId).collect { _participants.value = it } }
        viewModelScope.launch { repository.getAssignments(groupId).collect { _assignments.value = it } }
    }

    fun addParticipant(firstName: String, lastName: String, phone: String) {
        viewModelScope.launch { repository.addParticipant(groupId, firstName, lastName, phone) }
    }

    fun deleteParticipant(participant: ParticipantEntity) {
        viewModelScope.launch { repository.deleteParticipant(participant) }
    }

    fun generateAssignments(onDone: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val people = repository.getParticipantsOnce(groupId)
            if (people.size < 2) {
                onDone(false, "Need at least 2 participants")
                return@launch
            }

            val exclusions = repository.getExclusionsOnce(groupId)

            // Build the previous-year forbidden set if this group was duplicated
            val previousYearForbidden = buildPreviousYearForbidden(people)

            try {
                val pairs = DerangementUtil.generate(people, exclusions, previousYearForbidden)
                repository.saveAssignments(groupId, pairs)
                val prevNote = if (previousYearForbidden.isNotEmpty()) " (avoiding last year's pairings)" else ""
                onDone(true, "Assignments generated for ${people.size} participants$prevNote")
            } catch (e: IllegalStateException) {
                onDone(false, e.message ?: "Could not generate valid assignments")
            }
        }
    }

    /**
     * If this group has a previousGroupId, looks up last year's assignments and maps them
     * to current participant IDs via sourceParticipantId, returning a set of (giverId, receiverId)
     * pairs that should not be repeated this year.
     */
    private suspend fun buildPreviousYearForbidden(
        currentParticipants: List<ParticipantEntity>
    ): Set<Pair<Long, Long>> {
        val prevGroupId = _group.value?.previousGroupId ?: return emptySet()

        // Map: old participant ID → new participant ID
        val oldToNew = currentParticipants
            .filter { it.sourceParticipantId != null }
            .associate { it.sourceParticipantId!! to it.id }

        if (oldToNew.isEmpty()) return emptySet()

        // Remap last year's (oldGiverId → oldReceiverId) to (newGiverId → newReceiverId)
        val rawPairs = repository.getRawAssignmentPairs(prevGroupId)
        return rawPairs.mapNotNull { pair ->
            val newGiver = oldToNew[pair.giverId]
            val newReceiver = oldToNew[pair.receiverId]
            if (newGiver != null && newReceiver != null) Pair(newGiver, newReceiver) else null
        }.toSet()
    }

    fun buildMessage(organizerName: String, year: Int, budgetDollars: Int?, a: AssignmentWithNames): String {
        val base = "Hey ${a.giverFirstName}, it's $organizerName. For the $year family Secret Santa, you have ${a.receiverFirstName} ${a.receiverLastName}."
        return if (budgetDollars != null) "$base Your budget is $$budgetDollars." else base
    }
}
