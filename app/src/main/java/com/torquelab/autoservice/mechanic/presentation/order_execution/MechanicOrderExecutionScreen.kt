package com.torquelab.autoservice.mechanic.presentation.order_execution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.mechanic.domain.model.MechanicProposeTaskInput
import com.torquelab.autoservice.mechanic.domain.model.MechanicTask
import com.torquelab.autoservice.mechanic.domain.model.MechanicTaskPartInput
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.workshop.domain.model.TaskStatus

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
        onProposeTask = viewModel::proposeTask
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
    onProposeTask: (MechanicProposeTaskInput) -> Unit
) {
    var showProposeDialog by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.CarRepair, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            val vehicleName = state.order?.vehicleName?.takeIf { it.isNotBlank() } ?: "Vehículo"
                            Text(text = vehicleName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            
                            val vehiclePlate = state.order?.vehiclePlate?.takeIf { it.isNotBlank() } ?: "Sin placa"
                            Text(text = vehiclePlate, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
                        Text(text = "Diagnóstico técnico", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
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
                    Column {
                        Text("Tareas técnicas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("Ejecute o proponga tareas", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showProposeDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Proponer")
                    }
                }
            }

            if (state.tasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No existen tareas registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.tasks) { task ->
                    TaskCard(
                        task = task,
                        onStart = { onStartTask(task.id) },
                        onComplete = { onCompleteTask(task.id) }
                    )
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
                showProposeDialog = false
            }
        )
    }
}

@Composable
fun TaskCard(
    task: MechanicTask,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = task.description, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                
                val statusText = when {
                    task.status == TaskStatus.PENDING && task.adminReviewStatus == "SUBMITTED" -> "ESPERANDO"
                    task.status == TaskStatus.IN_PROGRESS -> "EN EJECUCIÓN"
                    task.status == TaskStatus.COMPLETED -> "COMPLETADA"
                    else -> "PENDIENTE"
                }

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(text = task.priority, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${task.estimatedTime} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            if (task.parts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Materiales: ${task.parts.joinToString { "${it.quantity}x ${it.name}" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (task.status == TaskStatus.PENDING && task.adminReviewStatus != "SUBMITTED") {
                    Button(onClick = onStart) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("Iniciar")
                    }
                }
                if (task.status == TaskStatus.IN_PROGRESS) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Text("Completar")
                    }
                }
            }
        }
    }
}

@Composable
fun ProposeTaskDialog(
    inventoryItems: List<InventoryItem>,
    onDismiss: () -> Unit,
    onPropose: (String, String, Int, List<MechanicTaskPartInput>) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var estimatedTime by remember { mutableStateOf("60") }
    
    // Simplification for the prototype: Just text inputs.
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva propuesta técnica", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Prioridad (LOW/MEDIUM/HIGH)") }
                )
                OutlinedTextField(
                    value = estimatedTime,
                    onValueChange = { estimatedTime = it },
                    label = { Text("Tiempo estimado (min)") }
                )
                Text("Nota: Se enviará sin materiales adicionales en el prototipo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = {
                onPropose(
                    description,
                    priority,
                    estimatedTime.toIntOrNull() ?: 60,
                    emptyList() // No parts for prototype simplification
                )
            }) {
                Text("Enviar propuesta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
