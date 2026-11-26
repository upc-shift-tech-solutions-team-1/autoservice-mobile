package com.torquelab.autoservice.mechanic.presentation.order_execution

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.model.MechanicTaskPartInput
import com.torquelab.autoservice.mechanic.domain.model.TaskReport
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.shared.ui.components.OperationChoice
import com.torquelab.autoservice.shared.ui.components.OperationDialog
import com.torquelab.autoservice.workshop.domain.model.TaskMaterialInput
import com.torquelab.autoservice.workshop.domain.model.TaskStatus
import com.torquelab.autoservice.workshop.presentation.MaterialSelection

enum class TaskPriorityLevel(
    val label: String,
    val weight: Int,
    val containerColor: Color,
    val contentColor: Color
) {
    URGENT("URGENTE", 4, Color(0xFFFEE2E2), Color(0xFFDC2626)),
    HIGH("ALTA", 3, Color(0xFFFFEDD5), Color(0xFFC2410C)),
    MEDIUM("MEDIA", 2, Color(0xFFE0F2FE), Color(0xFF0369A1)),
    LOW("BAJA", 1, Color(0xFFF1F5F9), Color(0xFF475569));

    companion object {
        fun fromString(value: String?): TaskPriorityLevel {
            return when (value?.trim()?.uppercase()) {
                "URGENT", "URGENTE", "CRITICAL" -> URGENT
                "HIGH", "ALTA" -> HIGH
                "MEDIUM", "MEDIA" -> MEDIUM
                "LOW", "BAJA" -> LOW
                else -> MEDIUM
            }
        }
    }
}

@Composable
fun MechanicOrderExecutionRoute(
    onBack: () -> Unit,
    viewModel: MechanicOrderExecutionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mechanicId = viewModel.getCurrentMechanicId()

    MechanicOrderExecutionScreen(
        state = state,
        mechanicId = mechanicId,
        onBack = onBack,
        onRetry = viewModel::loadOrderData,
        onUpdateDiagnosis = viewModel::updateDiagnosisText,
        onStartTask = viewModel::startTask,
        onCompleteTask = viewModel::completeTask,
        onProposeTask = viewModel::proposeTask,
        onSaveReport = viewModel::saveReport
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicOrderExecutionScreen(
    state: MechanicOrderExecutionState,
    mechanicId: Int?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onUpdateDiagnosis: (String) -> Unit,
    onStartTask: (Int) -> Unit,
    onCompleteTask: (Int) -> Unit,
    onProposeTask: (MechanicProposeTaskInput) -> Unit,
    onSaveReport: ((Int, TaskReport, Boolean) -> Unit)? = null
) {
    var showProposeDialog by remember { mutableStateOf(false) }
    var reportTask by remember { mutableStateOf<MechanicTask?>(null) }
    var completeReport by remember { mutableStateOf(false) }
    val closed = state.order?.status in setOf("FINISHED", "DELIVERED", "CANCELLED")
    LaunchedEffect(state.savedVersion) { showProposeDialog = false; reportTask = null }

    if (state.isLoading) {
        AutoServiceLoadingState()
        return
    }

    if (state.error != null) {
        AutoServiceErrorState(
            title = "Error",
            description = state.error,
            retryText = "Reintentar",
            onRetry = onRetry
        )
        return
    }

    // Task prioritization and sorting for clear identification (EC-08)
    val sortedTasks = remember(state.tasks) {
        state.tasks.sortedWith(
            compareBy<MechanicTask> {
                when (it.status) {
                    TaskStatus.IN_PROGRESS -> 0
                    TaskStatus.PENDING -> 1
                    TaskStatus.COMPLETED -> 2
                    else -> 3
                }
            }.thenByDescending {
                TaskPriorityLevel.fromString(it.priority).weight
            }
        )
    }

    val nextPriorityTask = remember(state.tasks) {
        state.tasks.firstOrNull { it.status == TaskStatus.IN_PROGRESS }
            ?: state.tasks.filter { it.status == TaskStatus.PENDING }
                .maxByOrNull { TaskPriorityLevel.fromString(it.priority).weight }
    }

    val remainingTasks = remember(sortedTasks, nextPriorityTask) {
        if (nextPriorityTask != null) {
            sortedTasks.filter { it.id != nextPriorityTask.id }
        } else {
            sortedTasks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orden ${state.order?.trackingCode ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (state.isSubmitting) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            item {
                Text(
                    text = "Información de la orden",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CarRepair,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            val vehicleName = state.order?.vehicleName?.takeIf { it.isNotBlank() } ?: "Vehículo"
                            Text(
                                text = vehicleName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val vehiclePlate = state.order?.vehiclePlate?.takeIf { it.isNotBlank() } ?: "Sin placa"
                            Text(
                                text = vehiclePlate,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Diagnóstico técnico",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "El diagnóstico se adjuntará a las propuestas y tareas completadas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.diagnosisText,
                            onValueChange = onUpdateDiagnosis,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Describa las fallas...") },
                            maxLines = 4
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            "Tareas técnicas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Priorizadas según urgencia y estado",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showProposeDialog = true },
                        enabled = !closed && !state.isSubmitting && mechanicId != null,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Proponer")
                    }
                }
            }

            if (state.tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No existen tareas registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Highlight Next Priority Activity (EC-08)
                nextPriorityTask?.let { heroTask ->
                    item {
                        NextPriorityTaskHeroCard(
                            task = heroTask,
                            onStart = { onStartTask(heroTask.id) },
                            onComplete = {
                                if (onSaveReport == null) onCompleteTask(heroTask.id)
                                else { completeReport = true; reportTask = heroTask }
                            },
                            enabled = !closed && !state.isSubmitting && heroTask.mechanicId == mechanicId,
                            onReport = { completeReport = false; reportTask = heroTask }
                        )
                    }
                }

                if (remainingTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Otras tareas asignadas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(remainingTasks) { task ->
                        TaskCard(
                            task = task,
                            onStart = { onStartTask(task.id) },
                            onComplete = {
                                if (onSaveReport == null) onCompleteTask(task.id)
                                else { completeReport = true; reportTask = task }
                            },
                            enabled = !closed && !state.isSubmitting && task.mechanicId == mechanicId,
                            onReport = { completeReport = false; reportTask = task }
                        )
                    }
                }

                item {
                    OrderProgressBar(tasks = state.tasks)
                }
            }
        }
    }

    if (showProposeDialog) {
        ProposeTaskDialog(
            inventoryItems = state.inventoryItems,
            busy = state.isSubmitting,
            error = state.actionError,
            onDismiss = { showProposeDialog = false },
            onPropose = { description, priority, time, parts ->
                mechanicId?.let { mId ->
                    state.order?.id?.let { oId ->
                        onProposeTask(
                            MechanicProposeTaskInput(
                                workOrderId = oId,
                                mechanicId = mId,
                                description = description,
                                priority = priority,
                                estimatedTime = time,
                                technicalDiagnosis = state.diagnosisText,
                                parts = parts
                            )
                        )
                    }
                }
            }
        )
    }

    reportTask?.let { task ->
        var diagnosis by remember(task.id) { mutableStateOf(task.technicalDiagnosis.ifBlank { state.diagnosisText }) }
        var explanation by remember(task.id) { mutableStateOf(task.customerExplanation) }
        var observation by remember(task.id) { mutableStateOf(task.internalObservation) }
        var evidence by remember(task.id) { mutableStateOf(task.evidenceRegistered) }
        val report = TaskReport(diagnosis, explanation, observation, evidence)

        OperationDialog(
            if (completeReport) "Completar tarea" else "Informe de tarea",
            state.isSubmitting,
            state.actionError ?: report.validationError(),
            report.validationError() == null,
            { reportTask = null },
            { onSaveReport?.invoke(task.id, report, completeReport) }
        ) {
            OutlinedTextField(diagnosis, { diagnosis = it }, label = { Text("Diagnóstico técnico") })
            OutlinedTextField(explanation, { explanation = it }, label = { Text("Explicación para el cliente") })
            OutlinedTextField(observation, { observation = it }, label = { Text("Observación interna (opcional)") })
            OutlinedTextField(evidence, { evidence = it }, label = { Text("Enlace de evidencia HTTP/HTTPS (opcional)") })
        }
    }
}

@Composable
fun NextPriorityTaskHeroCard(
    task: MechanicTask,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    enabled: Boolean = true,
    onReport: (() -> Unit)? = null
) {
    val priority = TaskPriorityLevel.fromString(task.priority)
    val isExecuting = task.status == TaskStatus.IN_PROGRESS

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExecuting) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isExecuting) MaterialTheme.colorScheme.primary else priority.containerColor,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isExecuting) "ACTIVIDAD EN EJECUCIÓN" else "SIGUIENTE ACTIVIDAD PRIORITARIA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                PriorityChip(priorityString = task.priority)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = task.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatusBadge(status = task.status, adminReviewStatus = task.adminReviewStatus)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${task.estimatedTime} min",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (task.parts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Materiales: ${task.parts.joinToString { "${it.quantity}x ${it.name}" }}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.technicalDiagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Diagnóstico: ${task.technicalDiagnosis}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct Action Update Section (EC-08)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.status == TaskStatus.PENDING && task.adminReviewStatus == "APPROVED") {
                    Button(
                        onClick = onStart,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Iniciar Tarea",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else if (task.status == TaskStatus.IN_PROGRESS) {
                    if (onReport != null) {
                        OutlinedButton(
                            onClick = onReport,
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Informe")
                        }
                    }
                    Button(
                        onClick = onComplete,
                        enabled = enabled,
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Completar Tarea",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else if (task.status == TaskStatus.PENDING && task.adminReviewStatus == "SUBMITTED") {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Esperando aprobación del Administrador",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: MechanicTask,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    enabled: Boolean = true,
    onReport: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                PriorityChip(priorityString = task.priority)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatusBadge(status = task.status, adminReviewStatus = task.adminReviewStatus)

                Text(
                    text = "${task.estimatedTime} min",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            if (task.parts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Materiales: ${task.parts.joinToString { "${it.quantity}x ${it.name}" }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.technicalDiagnosis.isNotBlank()) Text("Diagnóstico: ${task.technicalDiagnosis}", fontSize = 12.sp)
            if (task.customerExplanation.isNotBlank()) Text("Explicación: ${task.customerExplanation}", fontSize = 12.sp)
            if (task.internalObservation.isNotBlank()) Text("Observación interna: ${task.internalObservation}", fontSize = 12.sp)
            if (task.evidenceRegistered.isNotBlank()) Text("Evidencia: ${task.evidenceRegistered}", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onReport != null && task.status == TaskStatus.IN_PROGRESS) {
                    TextButton(onClick = onReport, enabled = enabled) {
                        Text("Informe")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (task.status == TaskStatus.PENDING && task.adminReviewStatus == "APPROVED") {
                    Button(onClick = onStart, enabled = enabled) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar")
                    }
                }
                if (task.status == TaskStatus.IN_PROGRESS) {
                    Button(
                        onClick = onComplete,
                        enabled = enabled,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Completar")
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityChip(priorityString: String, modifier: Modifier = Modifier) {
    val priority = TaskPriorityLevel.fromString(priorityString)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = priority.containerColor,
        modifier = modifier
    ) {
        Text(
            text = "PRIORIDAD ${priority.label}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = priority.contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TaskStatusBadge(status: TaskStatus, adminReviewStatus: String, modifier: Modifier = Modifier) {
    val (label, containerColor, contentColor) = when {
        status == TaskStatus.PENDING && adminReviewStatus == "SUBMITTED" ->
            Triple("ESPERANDO", Color(0xFFEDE9FE), Color(0xFF6D28D9))
        status == TaskStatus.IN_PROGRESS ->
            Triple("EN EJECUCIÓN", Color(0xFFFEF3C7), Color(0xFFB45309))
        status == TaskStatus.COMPLETED ->
            Triple("COMPLETADA", Color(0xFFDCFCE7), Color(0xFF15803D))
        status == TaskStatus.PENDING && adminReviewStatus == "APPROVED" ->
            Triple("LISTO PARA INICIAR", Color(0xFFE0F2FE), Color(0xFF0369A1))
        else ->
            Triple("PENDIENTE", Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProposeTaskDialog(
    inventoryItems: List<InventoryItem>,
    onDismiss: () -> Unit,
    busy: Boolean = false,
    error: String? = null,
    onPropose: (String, String, Int, List<MechanicTaskPartInput>) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var estimatedTime by remember { mutableStateOf("60") }
    var parts by remember { mutableStateOf(emptyList<TaskMaterialInput>()) }
    OperationDialog(
        "Nueva propuesta técnica",
        busy,
        error,
        description.isNotBlank() && (estimatedTime.toIntOrNull() ?: 0) > 0,
        onDismiss,
        {
            onPropose(description.trim(), priority, estimatedTime.toInt(), parts.map { MechanicTaskPartInput(it.inventoryItemId, it.quantity) })
        },
        "Enviar propuesta"
    ) {
        OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
        OperationChoice("Prioridad", priority, listOf("LOW", "MEDIUM", "HIGH", "URGENT").map { it to it }, { priority = it })
        OutlinedTextField(estimatedTime, { estimatedTime = it }, label = { Text("Tiempo estimado (min)") })
        MaterialSelection(inventoryItems, parts, { parts = it })
        Text("El administrador cotiza y aprueba la propuesta antes de iniciarla.")
    }
}

@Composable
fun OrderProgressBar(tasks: List<MechanicTask>) {
    val completed = tasks.count { it.status == TaskStatus.COMPLETED }
    val total = tasks.size
    val progressPercent = if (total > 0) (completed.toFloat() / total.toFloat()) else 0f
    val progressPercentInt = (progressPercent * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tareas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$completed / $total", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progreso", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$progressPercentInt%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (progressPercent > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF5CB85C)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (progressPercent > 0.1f) {
                            Text(
                                text = "$progressPercentInt%",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MechanicOrderExecutionScreenPreview() {
    val sampleOrder = com.torquelab.autoservice.mechanic.domain.model.MechanicOrder(
        id = 1,
        trackingCode = "TRK-001",
        vehicleId = 1,
        customerId = 1,
        mechanicId = 1,
        description = "Mantenimiento preventivo",
        status = "IN_PROGRESS",
        price = 350.0,
        vehicleName = "Toyota Hilux 2022",
        vehiclePlate = "ABC-123"
    )

    val sampleTasks = listOf(
        MechanicTask(
            id = 101,
            workOrderId = 1,
            description = "Cambio de pastillas y rectificado de discos",
            status = TaskStatus.IN_PROGRESS,
            priority = "HIGH",
            estimatedTime = 45,
            laborPrice = 120.0,
            materialsCost = 150.0,
            technicalDiagnosis = "Pastillas desgastadas al 10%",
            adminReviewStatus = "APPROVED",
            parts = emptyList(),
            mechanicId = 1
        ),
        MechanicTask(
            id = 102,
            workOrderId = 1,
            description = "Cambio de filtro de aceite y lubricación",
            status = TaskStatus.PENDING,
            priority = "URGENT",
            estimatedTime = 30,
            laborPrice = 50.0,
            materialsCost = 40.0,
            technicalDiagnosis = "",
            adminReviewStatus = "APPROVED",
            parts = emptyList(),
            mechanicId = 1
        )
    )

    MechanicOrderExecutionScreen(
        state = MechanicOrderExecutionState(
            order = sampleOrder,
            tasks = sampleTasks,
            diagnosisText = "Inspección de frenos y sistema hidráulico realizada."
        ),
        mechanicId = 1,
        onBack = {},
        onRetry = {},
        onUpdateDiagnosis = {},
        onStartTask = {},
        onCompleteTask = {},
        onProposeTask = {}
    )
}

