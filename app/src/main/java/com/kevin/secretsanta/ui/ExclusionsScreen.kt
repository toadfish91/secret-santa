package com.kevin.secretsanta.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kevin.secretsanta.data.ParticipantEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExclusionsScreen(
    groupId: Long,
    factory: ViewModelFactory,
    onBack: () -> Unit
) {
    val viewModel: ExclusionsViewModel = viewModel(factory = factory)
    LaunchedEffect(groupId) { viewModel.init(groupId) }

    val participants by viewModel.participants.collectAsState()
    val exclusions by viewModel.exclusions.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exclusions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (participants.size < 2) snackbarMessage = "Add at least 2 participants first"
                    else showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add exclusion")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (exclusions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No exclusions yet.\nTap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(exclusions, key = { it.id }) { exclusion ->
                        val nameA = viewModel.participantName(exclusion.participantIdA)
                        val nameB = viewModel.participantName(exclusion.participantIdB)
                        ListItem(
                            headlineContent = {
                                if (exclusion.twoWay) {
                                    // Two-way: neither can get the other
                                    Text("$nameA  ↔  $nameB")
                                } else {
                                    // One-way: A cannot get B, but B can get A
                                    Text("$nameA  →  $nameB")
                                }
                            },
                            supportingContent = {
                                if (exclusion.twoWay) {
                                    Text("Neither can be assigned to the other")
                                } else {
                                    Text("$nameA cannot get $nameB  •  $nameB can still get $nameA")
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteExclusion(exclusion.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove exclusion")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddExclusionDialog(
            participants = participants,
            onDismiss = { showDialog = false },
            onAdd = { idA, idB, twoWay ->
                viewModel.addExclusion(idA, idB, twoWay)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExclusionDialog(
    participants: List<ParticipantEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long, Long, Boolean) -> Unit
) {
    var selectedA by remember { mutableStateOf<ParticipantEntity?>(null) }
    var selectedB by remember { mutableStateOf<ParticipantEntity?>(null) }
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }
    var twoWay by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exclusion") },
        text = {
            Column {
                // Two-way / One-way toggle
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = twoWay,
                        onClick = { twoWay = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("Two-way  ↔") }
                    )
                    SegmentedButton(
                        selected = !twoWay,
                        onClick = { twoWay = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("One-way  →") }
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (twoWay)
                        "Neither person can be assigned to the other."
                    else
                        "Person A cannot get Person B, but Person B can still get Person A.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Dropdown A
                ExposedDropdownMenuBox(
                    expanded = expandedA,
                    onExpandedChange = { expandedA = it }
                ) {
                    OutlinedTextField(
                        value = selectedA?.let { "${it.firstName} ${it.lastName}" } ?: "Select person",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (twoWay) "Person A" else "Cannot give  (A)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                        participants.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.firstName} ${p.lastName}") },
                                onClick = { selectedA = p; expandedA = false; error = "" }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Direction label between the two dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (twoWay) "cannot get" else "cannot get →",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    HorizontalDivider(Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

                // Dropdown B
                ExposedDropdownMenuBox(
                    expanded = expandedB,
                    onExpandedChange = { expandedB = it }
                ) {
                    OutlinedTextField(
                        value = selectedB?.let { "${it.firstName} ${it.lastName}" } ?: "Select person",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (twoWay) "Person B" else "Cannot receive  (B)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                        participants.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.firstName} ${p.lastName}") },
                                onClick = { selectedB = p; expandedB = false; error = "" }
                            )
                        }
                    }
                }

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    selectedA == null || selectedB == null -> error = "Please select both people"
                    selectedA!!.id == selectedB!!.id -> error = "A person can't be excluded from themselves"
                    else -> onAdd(selectedA!!.id, selectedB!!.id, twoWay)
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
