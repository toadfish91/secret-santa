package com.kevin.secretsanta.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kevin.secretsanta.data.AssignmentWithNames
import com.kevin.secretsanta.data.ParticipantEntity
import com.kevin.secretsanta.util.SmsUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    factory: ViewModelFactory,
    onEditParticipant: (Long) -> Unit,
    onManageExclusions: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: GroupDetailViewModel = viewModel(factory = factory)
    LaunchedEffect(groupId) { viewModel.init(groupId) }

    val group by viewModel.group.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val assignments by viewModel.assignments.collectAsState()

    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var lookupParticipant by remember { mutableStateOf<ParticipantEntity?>(null) }
    var lookupResult by remember { mutableStateOf<AssignmentWithNames?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingAction?.invoke() else snackbarMessage = "SMS permission denied"
        pendingAction = null
    }

    fun runWithSmsPermission(action: () -> Unit) {
        if (SmsUtil.hasSmsPermission(context)) action()
        else { pendingAction = action; smsPermissionLauncher.launch(Manifest.permission.SEND_SMS) }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); snackbarMessage = null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.title ?: "Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add participant")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // Budget banner — only shown when a budget is set
            group?.budgetDollars?.let { budget ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "Budget: \$$budget per person",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Row 1: Generate + Resend All
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        viewModel.generateAssignments { _, message -> snackbarMessage = message }
                    },
                    enabled = participants.size >= 2
                ) {
                    Text(if (assignments.isEmpty()) "Generate Assignments" else "Re-generate")
                }
                Spacer(Modifier.width(8.dp))
                if (assignments.isNotEmpty()) {
                    OutlinedButton(onClick = {
                        runWithSmsPermission {
                            val g = group ?: return@runWithSmsPermission
                            assignments.forEach { a ->
                                SmsUtil.sendSms(context, a.giverPhone, viewModel.buildMessage(g.organizerName, g.year, g.budgetDollars, a))
                            }
                            snackbarMessage = "Sent to all participants"
                        }
                    }) { Text("Resend All") }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Row 2: Manage Exclusions
            OutlinedButton(
                onClick = onManageExclusions,
                enabled = participants.size >= 2
            ) {
                Text("Manage Exclusions")
            }

            Spacer(Modifier.height(16.dp))
            Text("Participants (${participants.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.fillMaxSize()) {
                items(participants, key = { it.id }) { p ->
                    val assignment = assignments.find { it.giverId == p.id }
                    ListItem(
                        headlineContent = { Text("${p.firstName} ${p.lastName}") },
                        supportingContent = { Text(p.phoneNumber) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { onEditParticipant(p.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    lookupParticipant = p
                                    lookupResult = assignment
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "View assignment")
                                }
                                if (assignment != null) {
                                    IconButton(onClick = {
                                        runWithSmsPermission {
                                            val g = group ?: return@runWithSmsPermission
                                            SmsUtil.sendSms(context, assignment.giverPhone, viewModel.buildMessage(g.organizerName, g.year, g.budgetDollars, assignment))
                                            snackbarMessage = "Sent to ${assignment.giverFirstName}"
                                        }
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Resend")
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteParticipant(p) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddParticipantDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { first, last, phone ->
                viewModel.addParticipant(first, last, phone)
                showAddDialog = false
            }
        )
    }

    lookupParticipant?.let { p ->
        AlertDialog(
            onDismissRequest = { lookupParticipant = null },
            title = { Text("${p.firstName} has...") },
            text = {
                Text(
                    lookupResult?.let { "${it.receiverFirstName} ${it.receiverLastName}" }
                        ?: "No assignment yet. Generate assignments first."
                )
            },
            confirmButton = { TextButton(onClick = { lookupParticipant = null }) { Text("Close") } }
        )
    }
}

@Composable
fun AddParticipantDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Participant") },
        text = {
            Column {
                OutlinedTextField(value = first, onValueChange = { first = it }, label = { Text("First name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = last, onValueChange = { last = it }, label = { Text("Last name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (first.isNotBlank() && phone.isNotBlank()) onAdd(first.trim(), last.trim(), phone.trim())
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
