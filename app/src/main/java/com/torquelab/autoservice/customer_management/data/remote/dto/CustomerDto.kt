package com.torquelab.autoservice.customer_management.data.remote.dto

import com.torquelab.autoservice.customer_management.domain.model.Customer

data class CustomerDto(
    val id: String,
    val workshopId: String,
    val fullName: String,
    val dni: String,
    val email: String,
    val phone: String
) {
    fun toDomain() = Customer(
        id = id,
        workshopId = workshopId,
        fullName = fullName,
        dni = dni,
        email = email,
        phone = phone
    )

    companion object {
        fun fromDomain(customer: Customer) = CustomerDto(
            id = customer.id,
            workshopId = customer.workshopId,
            fullName = customer.fullName,
            dni = customer.dni,
            email = customer.email,
            phone = customer.phone
        )
    }
}
