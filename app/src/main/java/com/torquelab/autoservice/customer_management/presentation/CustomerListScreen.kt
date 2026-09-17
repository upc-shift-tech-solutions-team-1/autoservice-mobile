package com.torquelab.autoservice.customer_management.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.customer_management.domain.model.Customer
import com.torquelab.autoservice.shared.ui.components.AutoServiceCard
import com.torquelab.autoservice.shared.ui.components.AutoServiceConfirmationDialog
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorState
import com.torquelab.autoservice.shared.ui.components.AutoServiceLoadingState
import com.torquelab.autoservice.shared.ui.components.AutoServiceTextField

@Composable
fun CustomerListRoute(
    onBack: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    CustomerListScreen(
        uiState = uiState,
        onBack = onBack,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAddCustomerClick = viewModel::showAddDialog,
        onEditCustomer = viewModel::showEditDialog,
        onDeleteCustomer = viewModel::onDeleteCustomer,
        onConfirmSave = viewModel::onSaveCustomer,
        onDismissDialog = viewModel::hideDialog,
        onRetry = viewModel::load
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    uiState: CustomerListUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddCustomerClick: () -> Unit,
    onEditCustomer: (Customer) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    onConfirmSave: (String, String, String, String) -> Unit,
    onDismissDialog: () -> Unit,
    onRetry: () -> Unit
) {
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.customers_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCustomerClick) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            AutoServiceTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                label = stringResource(R.string.customers_search_hint),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> AutoServiceLoadingState()
                uiState.error != null -> AutoServiceErrorState(
                    title = stringResource(R.string.error_connection),
                    description = uiState.error,
                    retryText = stringResource(R.string.common_retry),
                    onRetry = onRetry
                )

                uiState.customers.isEmpty() -> AutoServiceEmptyState(
                    title = stringResource(R.string.customers_empty_title),
                    description = stringResource(R.string.customers_empty_description)
                )

                else -> {
                    val filteredCustomers = uiState.customers.filter {
                        it.fullName.contains(uiState.searchQuery, ignoreCase = true) ||
                                it.dni.contains(uiState.searchQuery)
                    }
                    LazyColumn {
                        items(filteredCustomers) { customer ->
                            CustomerItem(
                                customer = customer,
                                onEdit = { onEditCustomer(customer) },
                                onDelete = { customerToDelete = customer }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (uiState.isDialogVisible) {
        CustomerFormDialog(
            customer = uiState.selectedCustomer,
            onDismiss = onDismissDialog,
            onConfirm = onConfirmSave
        )
    }

    customerToDelete?.let { customer ->
        AutoServiceConfirmationDialog(
            title = stringResource(R.string.common_delete),
            message = "Are you sure you want to delete ${customer.fullName}?",
            confirmText = stringResource(R.string.common_delete),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                onDeleteCustomer(customer.id)
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null }
        )
    }
}

@Composable
fun CustomerItem(
    customer: Customer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    AutoServiceCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = customer.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.customers_dni)}: ${customer.dni}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            if (customer.email.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = customer.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (customer.phone.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
                if (customer.phone.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${customer.phone}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
