package com.kevin.secretsanta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.secretsanta.data.ParticipantEntity
import com.kevin.secretsanta.data.SecretSantaRepository
import kotlinx.coroutines.launch

class EditParticipantViewModel(private val repository: SecretSantaRepository) : ViewModel() {

    suspend fun load(id: Long): ParticipantEntity? = repository.getParticipantById(id)

    fun save(participant: ParticipantEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateParticipant(participant)
            onDone()
        }
    }
}
