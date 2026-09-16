package com.torquelab.autoservice.customer_management.presentation

import com.torquelab.autoservice.customer_management.domain.model.Customer

data class CustomerListUiState(
    val isLoading: Boolean = false,
    val customers: List<Customer> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val isDialogVisible: Boolean = false,
    val selectedCustomer: Customer? = null
)
