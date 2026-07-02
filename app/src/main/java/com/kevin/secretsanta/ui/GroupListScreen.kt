package com.kevin.secretsanta.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kevin.secretsanta.data.GroupEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(factory: ViewModelFactory, onGroupClick: (Long) -> Unit) {
    val viewModel: GroupListViewModel = viewModel(factory = factory)
    val groups by viewModel.groups.collectAsState()
    var showNewDialog by remember { mutableStateOf(false) }
    var groupToDuplicate by remember { mutableStateOf<GroupEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Secret Santa Groups") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New group")
            }
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No groups yet. Tap + to create one.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(groups, key = { it.id }) { group ->
                    ListItem(
                        headlineContent = { Text(group.title) },
                        supportingContent = {
                            val budgetText = group.budgetDollars?.let { "  •  \$$it budget" } ?: ""
                            Text("${group.year}  •  ${group.organizerName}$budgetText")
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { groupToDuplicate = group }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate group")
                                }
                                IconButton(onClick = { viewModel.deleteGroup(group) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete group")
                                }
                            }
                        },
                        modifier = Modifier.clickable { onGroupClick(group.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showNewDialog) {
        GroupFormDialog(
            title = "New Secret Santa Group",
            initialOrganizer = "",
            onDismiss = { showNewDialog = false },
            onConfirm = { title, year, organizer, budget ->
                viewModel.createGroup(title, year, organizer, budget) { newId ->
                    showNewDialog = false
                    onGroupClick(newId)
                }
            }
        )
    }

    groupToDuplicate?.let { source ->
        GroupFormDialog(
            title = "Duplicate \"${source.title}\"",
            initialTitle = "${source.title} ${source.year + 1}",
            initialYear = source.year + 1,
            initialOrganizer = source.organizerName,
            initialBudget = source.budgetDollars?.toString() ?: "",
            confirmLabel = "Duplicate",
            onDismiss = { groupToDuplicate = null },
            onConfirm = { title, year, organizer, budget ->
                viewModel.duplicateGroup(source.id, title, year, organizer, budget) { newId ->
                    groupToDuplicate = null
                    onGroupClick(newId)
                }
            }
        )
    }
}

@Composable
fun GroupFormDialog(
    title: String,
    initialTitle: String = "",
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    initialOrganizer: String = "",
    initialBudget: String = "",
    confirmLabel: String = "Create",
    onDismiss: () -> Unit,
    onConfirm: (title: String, year: Int, organizer: String, budgetDollars: Int?) -> Unit
) {
    var groupTitle by remember { mutableStateOf(initialTitle) }
    var year by remember { mutableStateOf(initialYear.toString()) }
    var organizer by remember { mutableStateOf(initialOrganizer) }
    var budget by remember { mutableStateOf(initialBudget) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = groupTitle,
                    onValueChange = { groupTitle = it },
                    label = { Text("Group title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it.filter { c -> c.isDigit() } },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("Your name (used in messages)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { c -> c.isDigit() } },
                    label = { Text("Budget per person (optional)") },
                    placeholder = { Text("e.g. 50") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (groupTitle.isNotBlank()) {
                    onConfirm(
                        groupTitle.trim(),
                        year.toIntOrNull() ?: currentYear,
                        organizer.trim(),
                        budget.toIntOrNull()
                    )
                }
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
