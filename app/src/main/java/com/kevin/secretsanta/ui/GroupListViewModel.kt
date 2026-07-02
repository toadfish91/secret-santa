package com.kevin.secretsanta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.secretsanta.data.GroupEntity
import com.kevin.secretsanta.data.SecretSantaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupListViewModel(private val repository: SecretSantaRepository) : ViewModel() {

    val groups: StateFlow<List<GroupEntity>> =
        repository.groups.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createGroup(
        title: String,
        year: Int,
        organizerName: String,
        budgetDollars: Int?,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.createGroup(title, year, organizerName, budgetDollars)
            onCreated(id)
        }
    }

    fun duplicateGroup(
        sourceGroupId: Long,
        newTitle: String,
        newYear: Int,
        newOrganizerName: String,
        newBudgetDollars: Int?,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.duplicateGroup(sourceGroupId, newTitle, newYear, newOrganizerName, newBudgetDollars)
            onCreated(id)
        }
    }

    fun deleteGroup(group: GroupEntity) {
        viewModelScope.launch { repository.deleteGroup(group) }
    }
}
