package com.torquelab.autoservice.staff.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.ui.components.AutoServiceCard
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.shared.ui.theme.AutoServiceSpacing
import com.torquelab.autoservice.staff.domain.model.CreateMechanicInput
import com.torquelab.autoservice.staff.domain.model.Mechanic

@Composable
fun StaffRoute(viewModel: StaffViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) { viewModel.load() }
    val uiState by viewModel.uiState.collectAsState()

    StaffScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onRegister = viewModel::registerMechanic
    )
}

@Composable
fun StaffScreen(
    uiState: StaffUiState,
    onRetry: () -> Unit,
    onRegister: (CreateMechanicInput) -> Unit
) {
    var showRegisterDialog by remember { mutableStateOf(false) }
    var selectedMechanic by remember { mutableStateOf<Mechanic?>(null) }

    when {
        uiState.isLoading -> AutoServiceLoadingState()
        uiState.errorMessage != null -> AutoServiceErrorState(
            title = stringResource(R.string.common_error_title),
            description = uiState.errorMessage,
            retryText = stringResource(R.string.common_retry),
            onRetry = onRetry
        )
        else -> Column(
            modifier = Modifier.padding(AutoServiceSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.staff_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.staff_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { showRegisterDialog = true }) {
                    Text(stringResource(R.string.staff_add_mechanic))
                }
            }

            if (uiState.mechanics.isEmpty()) {
                AutoServiceEmptyState(
                    title = stringResource(R.string.staff_empty_title),
                    description = stringResource(R.string.staff_empty_description)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Medium)
                ) {
                    items(uiState.mechanics, key = { it.id }) { mechanic ->
                        MechanicCard(
                            mechanic = mechanic,
                            onClick = { selectedMechanic = mechanic }
                        )
                    }
                }
            }
        }
    }

    if (showRegisterDialog) {
        RegisterMechanicDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showRegisterDialog = false },
            onRegister = { input ->
                showRegisterDialog = false
                onRegister(input)
            }
        )
    }

    selectedMechanic?.let { mechanic ->
        MechanicDetailDialog(
            mechanic = mechanic,
            onDismiss = { selectedMechanic = null }
        )
    }
}

@Composable
private fun MechanicCard(mechanic: Mechanic, onClick: () -> Unit) {
    AutoServiceCard {
        Text(mechanic.fullName, style = MaterialTheme.typography.titleMedium)
        Text(mechanic.specialty, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(
                R.string.staff_card_summary,
                availabilityLabel(mechanic.isAvailable),
                mechanic.assignedTasks,
                mechanic.maxCapacity
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onClick) {
            Text(stringResource(R.string.staff_view_detail))
        }
    }
}

@Composable
private fun RegisterMechanicDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onRegister: (CreateMechanicInput) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("3") }
    val maxCapacity = capacity.toIntOrNull()
    val canRegister = fullName.isNotBlank() &&
        email.isNotBlank() &&
        specialty.isNotBlank() &&
        password.length >= 6 &&
        maxCapacity != null &&
        maxCapacity > 0 &&
        !isSubmitting

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.staff_add_mechanic)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Small)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(stringResource(R.string.staff_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.staff_email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text(stringResource(R.string.staff_specialty)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.staff_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.staff_capacity)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validCapacity = maxCapacity
                    if (validCapacity != null) {
                        onRegister(
                            CreateMechanicInput(
                                fullName = fullName.trim(),
                                email = email.trim(),
                                specialty = specialty.trim(),
                                maxCapacity = validCapacity,
                                password = password
                            )
                        )
                    }
                },
                enabled = canRegister
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun MechanicDetailDialog(mechanic: Mechanic, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mechanic.fullName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Small)) {
                Text("${stringResource(R.string.staff_email)}: ${mechanic.email}")
                Text("${stringResource(R.string.staff_specialty)}: ${mechanic.specialty}")
                Text(
                    stringResource(
                        R.string.staff_availability_detail,
                        availabilityLabel(mechanic.isAvailable)
                    )
                )
                Text(
                    stringResource(
                        R.string.staff_capacity_detail,
                        mechanic.assignedTasks,
                        mechanic.maxCapacity
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}

@Composable
private fun availabilityLabel(isAvailable: Boolean): String = stringResource(
    if (isAvailable) R.string.staff_available else R.string.staff_at_capacity
)
