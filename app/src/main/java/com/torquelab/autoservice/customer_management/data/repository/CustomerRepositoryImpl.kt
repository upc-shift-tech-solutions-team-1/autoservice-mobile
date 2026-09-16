package com.torquelab.autoservice.customer_management.data.repository

import com.torquelab.autoservice.customer_management.data.remote.CustomerApi
import com.torquelab.autoservice.customer_management.data.remote.dto.CustomerDto
import com.torquelab.autoservice.customer_management.domain.model.Customer
import com.torquelab.autoservice.customer_management.domain.repository.CustomerRepository
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val api: CustomerApi
) : CustomerRepository {

    override suspend fun getCustomers(): Result<List<Customer>> = runCatching {
        api.getCustomers().map { it.toDomain() }
    }

    override suspend fun getCustomerById(id: String): Result<Customer> = runCatching {
        api.getCustomer(id).toDomain()
    }

    override suspend fun createCustomer(customer: Customer): Result<Customer> = runCatching {
        api.createCustomer(CustomerDto.fromDomain(customer)).toDomain()
    }

    override suspend fun updateCustomer(customer: Customer): Result<Customer> = runCatching {
        api.updateCustomer(customer.id, CustomerDto.fromDomain(customer)).toDomain()
    }

    override suspend fun deleteCustomer(id: String): Result<Unit> = runCatching {
        api.deleteCustomer(id)
    }
}
