package com.torquelab.autoservice.workshop.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AttachMoney
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
    val backgroundColor = MaterialTheme.colorScheme.surface
    val borderColor = if (order.isRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = AutoServiceSpacing.Small),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Code + Risk Badge + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = order.trackingCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (order.isRisk) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.error,
                        ) {
                            Text(
                                text = "EN RIESGO",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Status Tag
                val (statusBg, statusText) = when (order.status) {
                    TaskStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                    TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    TaskStatus.DELIVERED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    TaskStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = taskStatusLabel(order.status).uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(
                    text = order.vehiclePlate.takeIf { it.isNotBlank() } ?: "Sin placa",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Customer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Text(
                    text = order.customerName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Dates and Total Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoBox(label = "Ingreso", value = order.startDate.takeIf { it.isNotBlank() } ?: "---", modifier = Modifier.weight(1f))
                InfoBox(label = "Entrega", value = order.estimatedDate.takeIf { it.isNotBlank() } ?: "---", modifier = Modifier.weight(1f))
                InfoBox(label = "Total", value = "S/ ${"%.2f".format(order.calculatedTotal)}", modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progreso de tareas", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${order.progress}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { order.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (order.tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tareas", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
    }
}

@Composable
fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TaskCard(
    task: WorkshopTask,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = AutoServiceSpacing.Small),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                val (statusBg, statusText) = when (task.status) {
                    TaskStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                    TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    TaskStatus.DELIVERED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    TaskStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = taskStatusLabel(task.status).uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mechanic
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(
                        text = task.mechanicName ?: stringResource(R.string.workshop_unassigned),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                // Time
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    val hours = task.estimatedMinutes / 60
                    val mins = task.estimatedMinutes % 60
                    val timeStr = if (hours > 0 && mins > 0) "${hours}h ${mins}m" else if (hours > 0) "${hours}h" else "${mins}m"
                    Text(
                        text = timeStr,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                // Priority
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text(
                        text = task.priority,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = "S/ ${"%.2f".format(task.laborPrice)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onUpdateStatus) {
                    Text(stringResource(R.string.workshop_status_action), fontSize = 12.sp)
                }
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.common_edit), fontSize = 12.sp)
                }
                TextButton(
                    onClick = onCancel,
                    enabled = task.status != TaskStatus.COMPLETED && task.status != TaskStatus.DELIVERED
                ) {
                    Text(
                        text = stringResource(R.string.common_delete), 
                        color = if (task.status != TaskStatus.COMPLETED && task.status != TaskStatus.DELIVERED) MaterialTheme.colorScheme.error else Color.Gray, 
                        fontSize = 12.sp
                    )
                }
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
