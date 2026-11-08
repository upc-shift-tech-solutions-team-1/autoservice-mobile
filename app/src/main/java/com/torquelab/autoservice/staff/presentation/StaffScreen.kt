package com.torquelab.autoservice.staff.presentation

import com.torquelab.autoservice.shared.ui.components.OperationDialog
import com.torquelab.autoservice.shared.ui.components.OperationChoice
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        onRegister = viewModel::registerMechanic,
        onUpdate = viewModel::updateMechanic, onDelete = viewModel::deleteMechanic
    )
}

@Composable
fun StaffScreen(
    uiState: StaffUiState,
    onRetry: () -> Unit,
    onRegister: (CreateMechanicInput) -> Unit,
    onUpdate: (Mechanic) -> Unit = {}, onDelete: (Int) -> Unit = {}
) {
    var showRegisterDialog by remember { mutableStateOf(false) }
    var selectedMechanic by remember { mutableStateOf<Mechanic?>(null) }
    var editingMechanic by remember { mutableStateOf<Mechanic?>(null) }
    var deletingMechanic by remember { mutableStateOf<Mechanic?>(null) }
    LaunchedEffect(uiState.savedVersion) { showRegisterDialog = false; selectedMechanic = null; editingMechanic = null; deletingMechanic = null }

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
                        Column {
                            MechanicCard(mechanic = mechanic, onClick = { selectedMechanic = mechanic })
                            Row {
                                TextButton(onClick = { editingMechanic = mechanic }) { Text("Editar") }
                                TextButton(onClick = { deletingMechanic = mechanic }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRegisterDialog) {
        RegisterMechanicDialog(
            isSubmitting = uiState.isSubmitting,
            error = uiState.errorMessage,
            onDismiss = { showRegisterDialog = false },
            onRegister = { input ->
                onRegister(input)
            }
        )
    }

    editingMechanic?.let { mechanic ->
        var name by remember(mechanic.id) { mutableStateOf(mechanic.fullName) }
        var email by remember(mechanic.id) { mutableStateOf(mechanic.email) }
        var specialty by remember(mechanic.id) { mutableStateOf(mechanic.specialty) }
        var capacity by remember(mechanic.id) { mutableStateOf(mechanic.maxCapacity.toString()) }
        OperationDialog("Editar mecánico", uiState.isSubmitting, uiState.errorMessage,
            name.isNotBlank() && email.contains('@') && specialty.isNotBlank() && (capacity.toIntOrNull() ?: 0) > 0,
            { editingMechanic = null }, { onUpdate(mechanic.copy(fullName = name.trim(), email = email.trim(), specialty = specialty.trim(), maxCapacity = capacity.toInt())) }) {
            OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
            OutlinedTextField(email, { email = it }, label = { Text("Correo") })
            SpecialtySelector(specialty, { specialty = it }, enabled = !uiState.isSubmitting)
            OutlinedTextField(capacity, { capacity = it }, label = { Text("Capacidad de tareas") })
        }
    }
    deletingMechanic?.let { mechanic ->
        OperationDialog("Eliminar mecánico", uiState.isSubmitting, uiState.errorMessage, true,
            { deletingMechanic = null }, { onDelete(mechanic.id) }, "Eliminar") {
            Text("¿Eliminar a ${mechanic.fullName}? El servidor verificará sus asignaciones.")
        }
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
    onRegister: (CreateMechanicInput) -> Unit,
    error: String? = null
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Small)
            ) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
                SpecialtySelector(specialty, { specialty = it }, enabled = !isSubmitting)
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
private fun SpecialtySelector(value: String, onChange: (String) -> Unit, enabled: Boolean) {
    val specialties = listOf(
        stringResource(R.string.staff_specialty_general),
        stringResource(R.string.staff_specialty_electrical),
        stringResource(R.string.staff_specialty_brakes),
        stringResource(R.string.staff_specialty_tires)
    )
    // Existing free-text values remain selectable when editing; never replace them silently.
    val choices = if (value.isNotBlank() && value !in specialties) specialties + value else specialties
    OperationChoice(
        label = stringResource(R.string.staff_specialty),
        value = value,
        choices = choices.map { it to it },
        onChange = onChange,
        enabled = enabled
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
