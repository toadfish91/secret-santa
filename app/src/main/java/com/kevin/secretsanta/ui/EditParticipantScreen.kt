package com.kevin.secretsanta.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kevin.secretsanta.data.ParticipantEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditParticipantScreen(participantId: Long, factory: ViewModelFactory, onDone: () -> Unit) {
    val viewModel: EditParticipantViewModel = viewModel(factory = factory)
    var participant by remember { mutableStateOf<ParticipantEntity?>(null) }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(participantId) {
        val p = viewModel.load(participantId)
        participant = p
        p?.let {
            first = it.firstName
            last = it.lastName
            phone = it.phoneNumber
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Edit Participant") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = first,
                onValueChange = { first = it },
                label = { Text("First name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = last,
                onValueChange = { last = it },
                label = { Text("Last name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                participant?.let {
                    viewModel.save(
                        it.copy(firstName = first.trim(), lastName = last.trim(), phoneNumber = phone.trim())
                    ) { onDone() }
                }
            }) { Text("Save") }
        }
    }
}
