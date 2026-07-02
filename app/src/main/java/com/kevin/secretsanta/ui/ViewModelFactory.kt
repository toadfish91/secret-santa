package com.kevin.secretsanta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kevin.secretsanta.data.SecretSantaRepository

class ViewModelFactory(private val repository: SecretSantaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GroupListViewModel::class.java) -> GroupListViewModel(repository) as T
            modelClass.isAssignableFrom(GroupDetailViewModel::class.java) -> GroupDetailViewModel(repository) as T
            modelClass.isAssignableFrom(EditParticipantViewModel::class.java) -> EditParticipantViewModel(repository) as T
            modelClass.isAssignableFrom(ExclusionsViewModel::class.java) -> ExclusionsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
