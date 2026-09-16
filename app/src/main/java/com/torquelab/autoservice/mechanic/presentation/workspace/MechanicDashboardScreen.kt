package com.torquelab.autoservice.mechanic.presentation.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.mechanic.domain.model.MechanicOrder
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState

@Composable
fun MechanicDashboardRoute(
    onOrderClick: (Int) -> Unit,
    viewModel: MechanicDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    MechanicDashboardScreen(
        state = state,
        onOrderClick = onOrderClick,
        onRetry = viewModel::fetchOrders
    )
}

@Composable
fun MechanicDashboardScreen(
    state: MechanicDashboardState,
    onOrderClick: (Int) -> Unit,
    onRetry: () -> Unit
) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MechanicHeader(
                name = state.mechanicName,
                specialty = state.mechanicSpecialty
            )
        }

        item {
            val pending = state.orders.count { it.status == "PENDING" }
            val inProgress = state.orders.count { it.status == "IN_PROGRESS" }
            val completed = state.orders.count { it.status == "FINISHED" }
            
            SummaryGrid(pending, inProgress, completed)
        }

        item {
            Text(
                text = "Órdenes asignadas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (state.orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay órdenes asignadas.", color = Color(0xFF64748B))
                }
            }
        } else {
            items(state.orders) { order ->
                MechanicOrderCard(
                    order = order,
                    onClick = { onOrderClick(order.id) }
                )
            }
        }
    }
}

@Composable
fun MechanicHeader(name: String, specialty: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "TU ESPACIO",
            color = Color(0xFF0B1680),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            fontSize = 12.sp
        )
        Text(
            text = "Workspace",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Text(
            text = "Gestiona y ejecuta las tareas de las órdenes asignadas a tu cargo.",
            color = Color(0xFF64748B),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(text = specialty, color = Color(0xFF64748B), fontSize = 14.sp)
                    Text(text = "Turno activo", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SummaryGrid(pending: Int, inProgress: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(title = "Pendientes", value = pending, modifier = Modifier.weight(1f))
        SummaryCard(title = "En progreso", value = inProgress, modifier = Modifier.weight(1f))
        SummaryCard(title = "Completadas", value = completed, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(title: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = Color(0xFF64748B), fontSize = 12.sp)
            Text(
                text = value.toString(),
                color = Color(0xFF0B1680),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MechanicOrderCard(order: MechanicOrder, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = order.trackingCode,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                val statusColor = when (order.status) {
                    "FINISHED" -> Color(0xFFDCFCE7)
                    "IN_PROGRESS" -> Color(0xFFE0F2FE)
                    else -> Color(0xFFFEF9C3)
                }
                val statusTextColor = when (order.status) {
                    "FINISHED" -> Color(0xFF166534)
                    "IN_PROGRESS" -> Color(0xFF075985)
                    else -> Color(0xFF854D0E)
                }
                val statusText = when (order.status) {
                    "FINISHED" -> "COMPLETADA"
                    "IN_PROGRESS" -> "EN PROGRESO"
                    else -> "PENDIENTE"
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = order.vehicleName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = order.description,
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${order.tasksCompleted} / ${order.totalTasks} tareas completadas",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { order.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00BFA5),
                trackColor = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Tareas", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = order.totalTasks.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Mano de obra", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = "S/ ${"%.2f".format(order.totalLaborCost)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0A2540))
            ) {
                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver orden")
            }
        }
    }
}
