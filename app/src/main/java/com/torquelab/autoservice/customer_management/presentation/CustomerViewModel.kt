package com.torquelab.autoservice.customer_management.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.customer_management.domain.model.Customer
import com.torquelab.autoservice.customer_management.domain.repository.CustomerRepository
import com.torquelab.autoservice.shared.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getCustomers()
                .onSuccess { customers ->
                    _uiState.update { it.copy(isLoading = false, customers = customers) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(isDialogVisible = true, selectedCustomer = null) }
    }

    fun showEditDialog(customer: Customer) {
        _uiState.update { it.copy(isDialogVisible = true, selectedCustomer = customer) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(isDialogVisible = false, selectedCustomer = null) }
    }

    fun onSaveCustomer(fullName: String, dni: String, phone: String, email: String) {
        val workshopId = sessionManager.currentSession()?.workshopId ?: return
        val currentCustomer = _uiState.value.selectedCustomer

        val customer = currentCustomer?.copy(
            fullName = fullName,
            dni = dni,
            phone = phone,
            email = email
        ) ?: Customer(
            workshopId = workshopId,
            fullName = fullName,
            dni = dni,
            phone = phone,
            email = email
        )

        viewModelScope.launch {
            val result = if (customer.id.isNotEmpty()) {
                repository.updateCustomer(customer)
            } else {
                repository.createCustomer(customer)
            }

            result.onSuccess {
                hideDialog()
                load()
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun onDeleteCustomer(customerId: String) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
