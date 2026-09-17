package com.torquelab.autoservice.workshop.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.torquelab.autoservice.shared.ui.components.AutoServiceConfirmationDialog
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.shared.ui.theme.AutoServiceSpacing
import com.torquelab.autoservice.staff.domain.model.Mechanic
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.domain.model.UpdateTaskInput
import com.torquelab.autoservice.workshop.domain.model.WorkshopTask
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder

@Composable
fun WorkshopRoute(viewModel: WorkshopViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) { viewModel.load() }
    val uiState by viewModel.uiState.collectAsState()

    WorkshopScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onCreateTask = viewModel::createTask,
        onUpdateTask = viewModel::updateTask,
        onCancelTask = viewModel::cancelTask,
        onUpdateTaskStatus = viewModel::updateTaskStatus
    )
}

@Composable
fun WorkshopScreen(
    uiState: WorkshopUiState,
    onRetry: () -> Unit,
    onCreateTask: (Int, String, String, Int, Double, Int?) -> Unit,
    onUpdateTask: (UpdateTaskInput) -> Unit,
    onCancelTask: (Int) -> Unit,
    onUpdateTaskStatus: (Int, TaskStatus) -> Unit
) {
    var showTaskForm by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<WorkshopTask?>(null) }
    var taskToCancel by remember { mutableStateOf<WorkshopTask?>(null) }
    var taskToUpdateStatus by remember { mutableStateOf<WorkshopTask?>(null) }

    when {
        uiState.isLoading -> AutoServiceLoadingState()
        uiState.errorMessage != null -> AutoServiceErrorState(
            title = stringResource(R.string.common_error_title),
            description = uiState.errorMessage,
            retryText = stringResource(R.string.common_retry),
            onRetry = onRetry
        )
        uiState.workOrders.isEmpty() -> AutoServiceEmptyState(
            title = stringResource(R.string.workshop_empty_title),
            description = stringResource(R.string.workshop_empty_description)
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
                        text = stringResource(R.string.workshop_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.workshop_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { showTaskForm = true }) {
                    Text(stringResource(R.string.workshop_add_task))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Medium)
            ) {
                items(uiState.workOrders, key = { it.id }) { order ->
                    WorkOrderCard(
                        order = order,
                        onEditTask = { taskToEdit = it },
                        onCancelTask = { taskToCancel = it },
                        onUpdateStatus = { taskToUpdateStatus = it }
                    )
                }
            }
        }
    }

    if (showTaskForm) {
        TaskEditorDialog(
            title = stringResource(R.string.workshop_add_task),
            orders = uiState.workOrders,
            mechanics = uiState.mechanics,
            task = null,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showTaskForm = false },
            onCreate = { orderId, description, priority, minutes, price, mechanicId ->
                showTaskForm = false
                onCreateTask(orderId, description, priority, minutes, price, mechanicId)
            },
            onUpdate = {}
        )
    }

    taskToEdit?.let { task ->
        TaskEditorDialog(
            title = stringResource(R.string.workshop_edit_task),
            orders = uiState.workOrders,
            mechanics = uiState.mechanics,
            task = task,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { taskToEdit = null },
            onCreate = { _, _, _, _, _, _ -> },
            onUpdate = { input ->
                taskToEdit = null
                onUpdateTask(input)
            }
        )
    }

    taskToCancel?.let { task ->
        AutoServiceConfirmationDialog(
            title = stringResource(R.string.workshop_cancel_task_title),
            message = stringResource(R.string.workshop_cancel_task_message),
            confirmText = stringResource(R.string.workshop_cancel_task),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                taskToCancel = null
                onCancelTask(task.id)
            },
            onDismiss = { taskToCancel = null }
        )
    }

    taskToUpdateStatus?.let { task ->
        StatusDialog(
            currentStatus = task.status,
            onDismiss = { taskToUpdateStatus = null },
            onStatusSelected = { status ->
                taskToUpdateStatus = null
                onUpdateTaskStatus(task.id, status)
            }
        )
    }
}

@Composable
private fun WorkOrderCard(
    order: WorkshopWorkOrder,
    onEditTask: (WorkshopTask) -> Unit,
    onCancelTask: (WorkshopTask) -> Unit,
    onUpdateStatus: (WorkshopTask) -> Unit
) {
    AutoServiceCard {
        Text(
            text = stringResource(R.string.workshop_order_summary, order.id, order.vehicleId),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = order.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.workshop_tracking_code, order.trackingCode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AutoServiceSpacing.Small))
        Text(
            text = stringResource(
                R.string.workshop_progress,
                order.progress,
                taskStatusLabel(order.status)
            ),
            style = MaterialTheme.typography.labelLarge
        )
        LinearProgressIndicator(
            progress = { order.progress / 100f },
            modifier = Modifier.fillMaxWidth()
        )
        order.tasks.forEach { task ->
            TaskCard(
                task = task,
                onEdit = { onEditTask(task) },
                onCancel = { onCancelTask(task) },
                onUpdateStatus = { onUpdateStatus(task) }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: WorkshopTask,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    AutoServiceCard(
        modifier = Modifier.padding(top = AutoServiceSpacing.Small),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(AutoServiceSpacing.Small)
    ) {
        Text(task.description, style = MaterialTheme.typography.titleSmall)
        Text(
            text = stringResource(
                R.string.workshop_task_summary,
                taskStatusLabel(task.status),
                task.estimatedMinutes,
                task.mechanicName ?: stringResource(R.string.workshop_unassigned)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.workshop_task_details, task.priority, task.laborPrice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Small)
        ) {
            TextButton(onClick = onUpdateStatus) {
                Text(stringResource(R.string.workshop_status_action))
            }
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.common_edit))
            }
            TextButton(
                onClick = onCancel,
                enabled = task.status != TaskStatus.COMPLETED &&
                    task.status != TaskStatus.DELIVERED
            ) {
                Text(stringResource(R.string.common_delete))
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    title: String,
    orders: List<WorkshopWorkOrder>,
    mechanics: List<Mechanic>,
    task: WorkshopTask?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onCreate: (Int, String, String, Int, Double, Int?) -> Unit,
    onUpdate: (UpdateTaskInput) -> Unit
) {
    var description by remember(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var priority by remember(task?.id) { mutableStateOf(task?.priority ?: "MEDIUM") }
    var minutes by remember(task?.id) { mutableStateOf(task?.estimatedMinutes?.toString() ?: "60") }
    var price by remember(task?.id) { mutableStateOf(task?.laborPrice?.toString() ?: "0") }
    var selectedOrderId by remember(task?.id) { mutableStateOf(task?.orderId ?: orders.firstOrNull()?.id) }
    var selectedMechanicId by remember(task?.id) { mutableStateOf(task?.mechanicId) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var mechanicExpanded by remember { mutableStateOf(false) }
    var orderExpanded by remember { mutableStateOf(false) }

    val validMinutes = minutes.toIntOrNull()
    val validPrice = price.toDoubleOrNull()
    val canSave = selectedOrderId != null &&
        description.isNotBlank() &&
        priority.isNotBlank() &&
        validMinutes != null && validMinutes > 0 &&
        validPrice != null && validPrice >= 0 &&
        !isSubmitting

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AutoServiceSpacing.Small)) {
                if (task == null) {
                    SelectionButton(
                        label = stringResource(R.string.workshop_order),
                        value = selectedOrderId?.let { "#$it" }
                            ?: stringResource(R.string.workshop_select_order),
                        expanded = orderExpanded,
                        onExpandedChange = { orderExpanded = it }
                    ) {
                        orders.forEach { order ->
                            DropdownMenuItem(
                                text = { Text("#${order.id}") },
                                onClick = {
                                    selectedOrderId = order.id
                                    orderExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.workshop_task_description)) },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectionButton(
                    label = stringResource(R.string.workshop_priority),
                    value = priority,
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = it }
                ) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                priority = option
                                priorityExpanded = false
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.workshop_estimated_minutes)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(stringResource(R.string.workshop_labor_price)) },
                    modifier = Modifier.fillMaxWidth()
                )
                SelectionButton(
                    label = stringResource(R.string.workshop_mechanic),
                    value = mechanics.firstOrNull { it.id == selectedMechanicId }?.fullName
                        ?: stringResource(R.string.workshop_unassigned),
                    expanded = mechanicExpanded,
                    onExpandedChange = { mechanicExpanded = it }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.workshop_unassigned)) },
                        onClick = {
                            selectedMechanicId = null
                            mechanicExpanded = false
                        }
                    )
                    mechanics.forEach { mechanic ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        R.string.staff_mechanic_option,
                                        mechanic.fullName,
                                        mechanic.specialty,
                                        mechanic.assignedTasks,
                                        mechanic.maxCapacity
                                    )
                                )
                            },
                            enabled = mechanic.isAvailable || mechanic.id == selectedMechanicId,
                            onClick = {
                                selectedMechanicId = mechanic.id
                                mechanicExpanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val orderId = selectedOrderId
                    val duration = validMinutes
                    val laborPrice = validPrice
                    if (orderId != null && duration != null && laborPrice != null) {
                        if (task == null) {
                            onCreate(orderId, description.trim(), priority, duration, laborPrice, selectedMechanicId)
                        } else {
                            onUpdate(
                                UpdateTaskInput(
                                    taskId = task.id,
                                    description = description.trim(),
                                    priority = priority,
                                    estimatedMinutes = duration,
                                    laborPrice = laborPrice,
                                    mechanicId = selectedMechanicId,
                                    status = task.status
                                )
                            )
                        }
                    }
                },
                enabled = canSave
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
private fun SelectionButton(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    menu: @Composable () -> Unit
) {
    Column {
        OutlinedButton(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$label: $value")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            menu()
        }
    }
}

@Composable
private fun StatusDialog(
    currentStatus: TaskStatus,
    onDismiss: () -> Unit,
    onStatusSelected: (TaskStatus) -> Unit
) {
    val statuses = listOf(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workshop_status_title)) },
        text = {
            Column {
                statuses.forEach { status ->
                    TextButton(
                        onClick = { onStatusSelected(status) },
                        enabled = status != currentStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(taskStatusLabel(status))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun taskStatusLabel(status: TaskStatus): String = when (status) {
    TaskStatus.PENDING -> stringResource(R.string.status_pending)
    TaskStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
    TaskStatus.COMPLETED -> stringResource(R.string.status_completed)
    TaskStatus.DELIVERED -> stringResource(R.string.status_delivered)
    TaskStatus.CANCELLED -> stringResource(R.string.status_cancelled)
    TaskStatus.UNKNOWN -> stringResource(R.string.status_unknown)
}
