package com.torquelab.autoservice.customer_trust.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTask
import com.torquelab.autoservice.shared.ui.components.AutoServiceCard
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.shared.ui.components.AutoServicePrimaryButton
import com.torquelab.autoservice.shared.ui.components.AutoServiceTextField

@Composable
fun TrackingRoute(
    onBack: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrackingViewScreen(
        uiState = uiState,
        onBack = onBack,
        onCodeChange = viewModel::onCodeChange,
        onSearch = viewModel::search
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingViewScreen(
    uiState: TrackingUiState,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tracking_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.tracking_description),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoServiceTextField(
                value = uiState.searchCode,
                onValueChange = onCodeChange,
                label = stringResource(R.string.tracking_code_hint),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AutoServicePrimaryButton(
                text = stringResource(R.string.tracking_check_status),
                onClick = onSearch,
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                uiState.isLoading -> AutoServiceLoadingState()
                uiState.error != null -> AutoServiceErrorState(
                    title = stringResource(R.string.tracking_order_not_found),
                    description = uiState.error,
                    onRetry = onSearch
                )
                uiState.order != null -> TrackingOrderDetail(order = uiState.order)
                else -> AutoServiceEmptyState(
                    title = stringResource(R.string.tracking_title),
                    description = stringResource(R.string.tracking_enter_code)
                )
            }
        }
    }
}

@Composable
fun TrackingOrderDetail(order: TrackingOrder) {
    LazyColumn {
        item {
            AutoServiceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Vehículo: ${order.vehicleModel}", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Placa: ${order.vehiclePlate}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Estado: ${order.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Progreso: ${order.progress}%", style = MaterialTheme.typography.labelLarge)
                    LinearProgressIndicator(
                        progress = { order.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = stringResource(R.string.tracking_estimated_delivery, order.estimatedDelivery))
                    Text(text = stringResource(R.string.tracking_total_cost, order.totalCost))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Etapas del Servicio", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(order.tasks) { task ->
            TrackingTaskItem(task = task)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            AutoServicePrimaryButton(
                text = stringResource(R.string.tracking_payment_action),
                onClick = { /* Simulate payment */ }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TrackingTaskItem(task: TrackingTask) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Timeline indicator placeholder
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Simple dot
            Canvas(modifier = Modifier.width(12.dp).height(12.dp)) {
                drawCircle(color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = task.description, style = MaterialTheme.typography.bodyLarge)
            Text(text = task.status, style = MaterialTheme.typography.bodySmall)
        }
    }
}
