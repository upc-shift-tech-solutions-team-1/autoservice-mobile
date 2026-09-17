package com.torquelab.autoservice.customer_management.domain.repository

import com.torquelab.autoservice.customer_management.domain.model.Customer

interface CustomerRepository {
    suspend fun getCustomers(): Result<List<Customer>>
    suspend fun getCustomerById(id: String): Result<Customer>
    suspend fun createCustomer(customer: Customer): Result<Customer>
    suspend fun updateCustomer(customer: Customer): Result<Customer>
    suspend fun deleteCustomer(id: String): Result<Unit>
}
