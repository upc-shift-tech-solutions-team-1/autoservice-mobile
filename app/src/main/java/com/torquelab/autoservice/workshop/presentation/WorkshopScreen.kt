package com.torquelab.autoservice.workshop.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.shared.ui.components.OperationChoice
import com.torquelab.autoservice.shared.ui.components.OperationDialog
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.workshop.domain.model.*

@Composable
fun WorkshopRoute(viewModel: WorkshopViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.uiState.collectAsState()
    WorkshopScreen(state, viewModel::load, viewModel::createTask, viewModel::updateTask,
        viewModel::cancelTask, viewModel::updateTaskStatus, viewModel::loadOptions,
        viewModel::createOrder, viewModel::updateOrder, viewModel::createTaskWithParts)
}

@Composable
fun WorkshopScreen(
    uiState: WorkshopUiState, onRetry: () -> Unit,
    onCreateTask: (Int, String, String, Int, Double, Int?) -> Unit,
    onUpdateTask: (UpdateTaskInput) -> Unit, onCancelTask: (Int) -> Unit,
    onUpdateTaskStatus: (Int, TaskStatus) -> Unit,
    onLoadOptions: () -> Unit = {}, onCreateOrder: (CreateWorkOrderInput) -> Unit = {},
    onUpdateOrder: (WorkshopWorkOrder, String?) -> Unit = { _, _ -> },
    onCreateTaskWithParts: ((CreateTaskInput) -> Unit)? = null
) {
    var createOrder by remember { mutableStateOf(false) }
    var createTask by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<WorkshopTask?>(null) }
    var deletingTask by remember { mutableStateOf<WorkshopTask?>(null) }
    var detailId by remember { mutableStateOf<Int?>(null) }
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    LaunchedEffect(uiState.savedVersion) { createOrder = false; createTask = false; editingTask = null; deletingTask = null }
    val active = uiState.workOrders.filterNot { it.closed }
    val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Órdenes de trabajo", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onLoadOptions(); createOrder = true }, enabled = !uiState.isSubmitting) { Text("Nueva orden") }
            OutlinedButton(onClick = { onLoadOptions(); createTask = true }, enabled = active.isNotEmpty() && !uiState.isSubmitting) { Text("Nueva tarea") }
        }
        OutlinedTextField(query, { query = it }, label = { Text("Buscar código, placa o cliente") }, modifier = Modifier.fillMaxWidth())
        OperationChoice("Estado", status, listOf("" to "Todos") + listOf("PENDING", "IN_PROGRESS", "FINISHED", "DELIVERED", "CANCELLED").map { it to it }, { status = it })
        if (uiState.isLoading || uiState.isSubmitting) LinearProgressIndicator(Modifier.fillMaxWidth())
        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(onClick = onRetry, enabled = !uiState.isSubmitting) { Text("Actualizar") }
        val visible = uiState.workOrders.filter { order ->
            (status.isEmpty() || order.status.toApiValue() == status) &&
                listOf(order.trackingCode, order.vehiclePlate, order.customerName).any { it.contains(query, true) }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (visible.isEmpty() && !uiState.isLoading) item { Text("No hay órdenes para mostrar. Cree un cliente, vehículo y mecánico antes de registrar una orden.") }
            items(visible, key = { it.id }) { order ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(order.trackingCode, style = MaterialTheme.typography.titleLarge)
                        Text("${order.vehiclePlate} · ${order.customerName}")
                        Text("${order.description} · ${order.status.toApiValue()}")
                        Text("Entrega estimada: ${order.estimatedDate} · Total: S/ ${"%.2f".format(order.calculatedTotal)}")
                        LinearProgressIndicator(progress = { order.progress / 100f }, modifier = Modifier.fillMaxWidth())
                        Row {
                            TextButton(onClick = { detailId = order.id }) { Text("Detalle / calidad") }
                            TextButton(onClick = { clipboard.setText(AnnotatedString(order.trackingCode)) }) { Text("Copiar seguimiento") }
                        }
                        order.tasks.forEach { task ->
                            HorizontalDivider()
                            Text(task.description, style = MaterialTheme.typography.titleMedium)
                            Text("${task.status.toApiValue()} · ${task.adminReviewStatus} · ${task.mechanicName ?: "Sin asignar"}")
                            Text("${task.estimatedMinutes} min · S/ ${"%.2f".format(task.laborPrice + task.materialsCost)}")
                            task.parts.forEach { Text("${it.quantity} × ${it.name}") }
                            task.technicalDiagnosis.takeIf(String::isNotBlank)?.let { Text("Diagnóstico: $it") }
                            task.customerExplanation.takeIf(String::isNotBlank)?.let { Text("Explicación: $it") }
                            task.internalObservation.takeIf(String::isNotBlank)?.let { Text("Observación interna: $it") }
                            task.evidenceRegistered.takeIf(String::isNotBlank)?.let { Text("Evidencia: $it") }
                            Row {
                                TextButton(onClick = { editingTask = task }, enabled = !order.closed && !uiState.isSubmitting) { Text("Editar / cotizar") }
                                val next = when {
                                    task.status == TaskStatus.PENDING && task.adminReviewStatus == "APPROVED" -> TaskStatus.IN_PROGRESS
                                    task.status == TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
                                    else -> null
                                }
                                if (next != null) TextButton(onClick = { onUpdateTaskStatus(task.id, next) }, enabled = !order.closed && !uiState.isSubmitting) { Text(if (next == TaskStatus.IN_PROGRESS) "Iniciar" else "Completar") }
                                TextButton(onClick = { deletingTask = task }, enabled = !order.closed && !uiState.isSubmitting && task.status !in setOf(TaskStatus.COMPLETED, TaskStatus.DELIVERED)) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }
    if (createOrder) OrderCreateDialog(uiState, { createOrder = false }, onCreateOrder)
    if (createTask || editingTask != null) TaskFormDialog(uiState, editingTask, { createTask = false; editingTask = null }, { input ->
        if (onCreateTaskWithParts != null) onCreateTaskWithParts(input)
        else onCreateTask(input.orderId, input.description, input.priority, input.estimatedMinutes, input.laborPrice, input.mechanicId)
    }, onUpdateTask)
    deletingTask?.let { task ->
        AlertDialog(onDismissRequest = { deletingTask = null }, title = { Text("Eliminar tarea") }, text = { Text("Se eliminará ${task.description}. Esta acción será procesada por el taller.") },
            confirmButton = { TextButton(onClick = { onCancelTask(task.id) }, enabled = !uiState.isSubmitting) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { deletingTask = null }) { Text("Cancelar") } })
    }
    uiState.workOrders.find { it.id == detailId }?.let { order ->
        OrderDetailDialog(order, uiState, { detailId = null }, onUpdateOrder)
    }
}

@Composable
private fun OrderCreateDialog(state: WorkshopUiState, onClose: () -> Unit, onSave: (CreateWorkOrderInput) -> Unit) {
    var vehicleId by remember { mutableStateOf(0) }
    var mechanicId by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    val vehicle = state.options.vehicles.find { it.id == vehicleId }
    val dateError = validateEstimatedDate(null, date, null)
    OperationDialog("Nueva orden", state.isSubmitting || state.optionsLoading, state.errorMessage,
        vehicle != null && vehicle.customerId > 0 && mechanicId > 0 && description.isNotBlank() && dateError == null,
        onClose, { onSave(CreateWorkOrderInput(vehicleId, vehicle!!.customerId, mechanicId, description.trim(), date)) }) {
        OperationChoice("Vehículo", vehicleId.toString(), state.options.vehicles.map { it.id.toString() to "${it.plate} · ${it.brand} ${it.model}" }, { vehicleId = it.toInt() })
        Text("Cliente: ${state.options.customers.find { it.id == vehicle?.customerId }?.fullName ?: "Seleccione un vehículo con cliente"}")
        OperationChoice("Mecánico principal", mechanicId.toString(), state.mechanics.filter { it.isAvailable }.map { it.id.toString() to "${it.fullName} · ${it.specialty}" }, { mechanicId = it.toInt() })
        OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
        OutlinedTextField(date, { date = it }, label = { Text("Fecha estimada (AAAA-MM-DD)") }, isError = date.isNotEmpty() && dateError != null)
        if (state.options.vehicles.isEmpty() || state.mechanics.isEmpty()) Text("Registre primero clientes, vehículos y mecánicos desde sus módulos.")
    }
}

@Composable
private fun OrderDetailDialog(order: WorkshopWorkOrder, state: WorkshopUiState, onClose: () -> Unit, onSave: (WorkshopWorkOrder, String?) -> Unit) {
    var date by remember(order.estimatedDate) { mutableStateOf(order.estimatedDate) }
    var reason by remember(order.estimatedDate) { mutableStateOf("") }
    var checks by remember(order.checklist) { mutableStateOf(order.checklist) }
    val dateError = validateEstimatedDate(order.estimatedDate, date, reason)
    OperationDialog("Orden ${order.trackingCode}", state.isSubmitting, state.errorMessage,
        !order.closed && dateError == null, onClose,
        { onSave(order.copy(estimatedDate = date, checklist = checks), reason) }) {
        Text("${order.description} · ${order.status.toApiValue()}")
        Text("Total oficial: S/ ${"%.2f".format(order.calculatedTotal)}")
        OutlinedTextField(date, { date = it }, label = { Text("Fecha estimada (AAAA-MM-DD)") }, enabled = !order.closed)
        if (date != order.estimatedDate) {
            OutlinedTextField(reason, { reason = it }, label = { Text("Motivo de la demora") }, enabled = !order.closed)
            dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        Text("Control de calidad", style = MaterialTheme.typography.titleMedium)
        listOf(
            Triple("Tareas completadas", checks.tasksCompleted, { value: Boolean -> checks = checks.copy(tasksCompleted = value) }),
            Triple("Repuestos verificados", checks.sparePartsChecked, { value: Boolean -> checks = checks.copy(sparePartsChecked = value) }),
            Triple("Diagnóstico validado", checks.diagnosisValidated, { value: Boolean -> checks = checks.copy(diagnosisValidated = value) }),
            Triple("Limpieza realizada", checks.cleaningDone, { value: Boolean -> checks = checks.copy(cleaningDone = value) }),
            Triple("Prueba final", checks.finalTestDone, { value: Boolean -> checks = checks.copy(finalTestDone = value) })
        ).forEach { (label, checked, change) ->
            Row { Checkbox(checked, change, enabled = !order.closed && !state.isSubmitting); Text(label, Modifier.padding(top = 12.dp)) }
        }
        if (!order.closed) {
            Text("Para finalizar: todas las tareas completadas y aprobadas, y los cinco controles de calidad.")
            Button(onClick = { onSave(order.copy(status = TaskStatus.FINISHED, checklist = checks, estimatedDate = date), reason) },
                enabled = order.canFinish(checks) && dateError == null && !state.isSubmitting) { Text("Finalizar orden") }
        }
        if (order.status == TaskStatus.FINISHED) Button(onClick = { onSave(order.copy(status = TaskStatus.DELIVERED), null) }, enabled = !state.isSubmitting) { Text("Entregar vehículo") }
    }
}

@Composable
internal fun MaterialSelection(items: List<InventoryItem>, selected: List<TaskMaterialInput>, onChange: (List<TaskMaterialInput>) -> Unit) {
    var itemId by remember { mutableStateOf(0) }
    var quantity by remember { mutableStateOf("1") }
    Text("Repuestos (se fijan al crear la tarea)", style = MaterialTheme.typography.titleSmall)
    selected.forEach { part ->
        Text("${part.quantity} × ${items.find { it.id == part.inventoryItemId }?.name ?: part.inventoryItemId}")
        TextButton(onClick = { onChange(selected.filterNot { it.inventoryItemId == part.inventoryItemId }) }) { Text("Quitar") }
    }
    OperationChoice("Repuesto", itemId.toString(), items.filter { candidate -> selected.none { it.inventoryItemId == candidate.id } }.map { it.id.toString() to "${it.name} · ${it.brand} · ${it.qualityTier} · stock ${it.stock}" }, { itemId = it.toInt() })
    OutlinedTextField(quantity, { quantity = it }, label = { Text("Cantidad") })
    TextButton(onClick = { onChange(selected + TaskMaterialInput(itemId, quantity.toInt())); itemId = 0; quantity = "1" },
        enabled = items.any { it.id == itemId } && selected.none { it.inventoryItemId == itemId } && (quantity.toIntOrNull() ?: 0) > 0) { Text("Agregar repuesto") }
    Text("El servidor valida y descuenta existencias al iniciar una tarea aprobada.")
}

@Composable
private fun TaskFormDialog(state: WorkshopUiState, task: WorkshopTask?, onClose: () -> Unit, onCreate: (CreateTaskInput) -> Unit, onUpdate: (UpdateTaskInput) -> Unit) {
    var description by remember { mutableStateOf(task?.description.orEmpty()) }
    var orderId by remember { mutableStateOf(task?.orderId ?: 0) }
    var mechanicId by remember { mutableStateOf(task?.mechanicId ?: 0) }
    var priority by remember { mutableStateOf(task?.priority ?: "MEDIUM") }
    var minutes by remember { mutableStateOf(task?.estimatedMinutes?.toString() ?: "60") }
    var price by remember { mutableStateOf(task?.laborPrice?.toString() ?: "0") }
    var cost by remember { mutableStateOf(task?.laborCost?.toString() ?: "0") }
    var parts by remember { mutableStateOf(emptyList<TaskMaterialInput>()) }
    val valid = state.workOrders.any { it.id == orderId && !it.closed } && description.isNotBlank() &&
        (minutes.toIntOrNull() ?: 0) > 0 && price.toBigDecimalOrNull()?.signum()?.let { it >= 0 } == true && cost.toBigDecimalOrNull()?.signum()?.let { it >= 0 } == true
    OperationDialog(if (task == null) "Nueva tarea" else "Editar / aprobar cotización", state.isSubmitting || state.optionsLoading,
        state.errorMessage, valid, onClose, {
            if (task == null) onCreate(CreateTaskInput(orderId, description.trim(), priority, minutes.toInt(), price.toDouble(), mechanicId.takeIf { it > 0 }, cost.toDouble(), parts))
            else onUpdate(UpdateTaskInput(task.id, description.trim(), priority, minutes.toInt(), price.toDouble(), mechanicId.takeIf { it > 0 }, task.status, cost.toDouble(), if (task.status == TaskStatus.PENDING) "APPROVED" else task.adminReviewStatus))
        }) {
        if (task == null) OperationChoice("Orden", orderId.toString(), state.workOrders.filterNot { it.closed }.map { it.id.toString() to it.trackingCode }, { orderId = it.toInt() })
        OutlinedTextField(description, { description = it }, label = { Text("Descripción de tarea") })
        OperationChoice("Prioridad", priority, listOf("LOW", "MEDIUM", "HIGH").map { it to it }, { priority = it })
        OutlinedTextField(minutes, { minutes = it }, label = { Text("Duración estimada (minutos)") })
        OutlinedTextField(price, { price = it }, label = { Text("Precio de mano de obra (S/)") })
        OutlinedTextField(cost, { cost = it }, label = { Text("Costo interno de mano de obra (S/)") })
        OperationChoice("Mecánico", mechanicId.toString(), listOf("0" to "Sin asignar") + state.mechanics.filter { it.isAvailable || it.id == mechanicId }.map { it.id.toString() to it.fullName }, { mechanicId = it.toInt() })
        if (task == null) MaterialSelection(state.options.inventory, parts, { parts = it })
        else {
            Text("Los repuestos iniciales se conservan al editar.")
            task.parts.forEach { Text("${it.quantity} × ${it.name}") }
        }
    }
}
