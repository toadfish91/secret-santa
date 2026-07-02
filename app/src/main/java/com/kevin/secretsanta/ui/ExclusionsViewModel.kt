package com.kevin.secretsanta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.secretsanta.data.ExclusionEntity
import com.kevin.secretsanta.data.ParticipantEntity
import com.kevin.secretsanta.data.SecretSantaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExclusionsViewModel(private val repository: SecretSantaRepository) : ViewModel() {

    private var groupId: Long = -1
    private var initialized = false

    private val _participants = MutableStateFlow<List<ParticipantEntity>>(emptyList())
    val participants: StateFlow<List<ParticipantEntity>> = _participants

    private val _exclusions = MutableStateFlow<List<ExclusionEntity>>(emptyList())
    val exclusions: StateFlow<List<ExclusionEntity>> = _exclusions

    fun init(groupId: Long) {
        if (initialized && this.groupId == groupId) return
        initialized = true
        this.groupId = groupId
        viewModelScope.launch { repository.getParticipants(groupId).collect { _participants.value = it } }
        viewModelScope.launch { repository.getExclusions(groupId).collect { _exclusions.value = it } }
    }

    fun addExclusion(idA: Long, idB: Long, twoWay: Boolean) {
        viewModelScope.launch { repository.addExclusion(groupId, idA, idB, twoWay) }
    }

    fun deleteExclusion(id: Long) {
        viewModelScope.launch { repository.deleteExclusion(id) }
    }

    fun participantName(id: Long): String {
        val p = _participants.value.find { it.id == id }
        return p?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"
    }
}
