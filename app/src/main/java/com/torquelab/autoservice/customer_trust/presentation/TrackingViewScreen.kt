package com.torquelab.autoservice.customer_trust.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderStatus
import com.torquelab.autoservice.customer_trust.domain.model.TrackingPart
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTask
import com.torquelab.autoservice.customer_trust.domain.model.TrackingTaskStatus
import com.torquelab.autoservice.shared.ui.components.AutoServiceCard
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.shared.ui.components.AutoServicePrimaryButton
import com.torquelab.autoservice.shared.ui.components.AutoServiceTextField
import java.text.NumberFormat
import java.util.Locale

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
        onSearch = viewModel::search,
        onResetSearch = viewModel::resetSearch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingViewScreen(
    uiState: TrackingUiState,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onResetSearch: () -> Unit
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
            if (uiState.order == null) {
                TrackingSearchForm(
                    uiState = uiState,
                    onCodeChange = onCodeChange,
                    onSearch = onSearch
                )
            }

            when {
                uiState.isLoading -> AutoServiceLoadingState(modifier = Modifier.weight(1f))
                uiState.error == TrackingUiError.EMPTY_CODE -> AutoServiceErrorState(
                    title = stringResource(R.string.tracking_code_required_title),
                    description = stringResource(R.string.tracking_code_required_description),
                    modifier = Modifier.weight(1f)
                )
                uiState.error == TrackingUiError.NOT_FOUND -> AutoServiceErrorState(
                    title = stringResource(R.string.tracking_order_not_found),
                    description = stringResource(R.string.tracking_not_found_description),
                    modifier = Modifier.weight(1f),
                    retryText = stringResource(R.string.common_retry),
                    onRetry = onSearch
                )
                uiState.error == TrackingUiError.CONNECTION -> AutoServiceErrorState(
                    title = stringResource(R.string.common_error_title),
                    description = stringResource(R.string.tracking_connection_error),
                    modifier = Modifier.weight(1f),
                    retryText = stringResource(R.string.common_retry),
                    onRetry = onSearch
                )
                uiState.order != null -> TrackingOrderDetail(
                    order = uiState.order,
                    onResetSearch = onResetSearch,
                    modifier = Modifier.weight(1f)
                )
                else -> AutoServiceEmptyState(
                    title = stringResource(R.string.tracking_title),
                    description = stringResource(R.string.tracking_enter_code),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TrackingSearchForm(
    uiState: TrackingUiState,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Text(
        text = stringResource(R.string.tracking_description),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    AutoServiceCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AutoServiceTextField(
                value = uiState.searchCode,
                onValueChange = onCodeChange,
                label = stringResource(R.string.tracking_code_hint),
                enabled = !uiState.isLoading,
                imeAction = ImeAction.Done,
                onDone = onSearch
            )

            AutoServicePrimaryButton(
                text = stringResource(R.string.tracking_check_status),
                onClick = onSearch,
                isLoading = uiState.isLoading
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun TrackingOrderDetail(
    order: TrackingOrder,
    onResetSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AutoServicePrimaryButton(
                text = stringResource(R.string.tracking_search_another),
                onClick = onResetSearch
            )
        }

        item {
            AutoServiceCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = order.trackingCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    order.customerName?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = stringResource(R.string.tracking_customer_name, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    order.workshopName?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = stringResource(R.string.tracking_workshop_name, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    val vehicleName = listOfNotNull(
                        order.vehicleBrand?.takeIf(String::isNotBlank),
                        order.vehicleModel?.takeIf(String::isNotBlank)
                    ).joinToString(" ")
                    if (vehicleName.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.tracking_vehicle, vehicleName),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    order.vehiclePlate?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = stringResource(R.string.tracking_vehicle_plate, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    order.serviceDescription?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = stringResource(R.string.tracking_service_description, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = stringResource(R.string.tracking_order_status, orderStatusLabel(order.status)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.tracking_progress, order.progress),
                        style = MaterialTheme.typography.labelLarge
                    )
                    LinearProgressIndicator(
                        progress = { order.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    order.estimatedDelivery?.takeIf(String::isNotBlank)?.let {
                        Text(
                            text = stringResource(R.string.tracking_estimated_delivery, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    order.totalCost?.let {
                        Text(
                            text = stringResource(R.string.tracking_total_cost, it.asCurrency()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (order.tasks.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.tracking_service_tasks),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(order.tasks, key = TrackingTask::id) { task ->
                TrackingTaskItem(task = task)
            }
        }
    }
}

@Composable
private fun TrackingTaskItem(task: TrackingTask) {
    AutoServiceCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.description,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = taskStatusLabel(task.status),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            task.customerExplanation?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = stringResource(R.string.tracking_customer_explanation, it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            task.technicalDiagnosis?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = stringResource(R.string.tracking_technical_diagnosis, it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            task.evidenceRegistered?.takeIf(String::isNotBlank)?.let {
                Text(
                    text = stringResource(R.string.tracking_evidence, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            task.laborPrice?.let {
                Text(
                    text = stringResource(R.string.tracking_labor_price, it.asCurrency()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (task.parts.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.tracking_materials),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                task.parts.forEach { part ->
                    TrackingPartItem(part = part)
                }
            }
        }
    }
}

@Composable
private fun TrackingPartItem(part: TrackingPart) {
    val amount = part.unitPrice?.times(part.quantity)?.asCurrency()
    val description = if (amount == null) {
        stringResource(R.string.tracking_material_quantity, part.name, part.quantity)
    } else {
        stringResource(R.string.tracking_material_with_price, part.name, part.quantity, amount)
    }
    Text(text = description, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun orderStatusLabel(status: TrackingOrderStatus): String = stringResource(
    when (status) {
        TrackingOrderStatus.PENDING -> R.string.status_pending
        TrackingOrderStatus.IN_PROGRESS -> R.string.status_in_progress
        TrackingOrderStatus.FINISHED -> R.string.tracking_stage_ready
        TrackingOrderStatus.DELIVERED -> R.string.status_delivered
        TrackingOrderStatus.CANCELLED -> R.string.status_cancelled
        TrackingOrderStatus.UNKNOWN -> R.string.status_unknown
    }
)

@Composable
private fun taskStatusLabel(status: TrackingTaskStatus): String = stringResource(
    when (status) {
        TrackingTaskStatus.PENDING -> R.string.status_pending
        TrackingTaskStatus.IN_PROGRESS -> R.string.status_in_progress
        TrackingTaskStatus.COMPLETED -> R.string.status_completed
        TrackingTaskStatus.DELIVERED -> R.string.status_delivered
        TrackingTaskStatus.CANCELLED -> R.string.status_cancelled
        TrackingTaskStatus.UNKNOWN -> R.string.status_unknown
    }
)

private fun Double.asCurrency(): String = NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(this)
