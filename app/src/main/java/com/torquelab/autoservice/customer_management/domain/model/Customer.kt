package com.torquelab.autoservice.customer_management.domain.model

data class Customer(
    val id: String = "",
    val workshopId: String,
    val fullName: String,
    val dni: String,
    val email: String,
    val phone: String
)
