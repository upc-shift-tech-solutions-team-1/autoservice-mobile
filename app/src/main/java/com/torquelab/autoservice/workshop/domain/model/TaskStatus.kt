package com.torquelab.autoservice.workshop.domain.model

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED,
    CANCELLED,
    UNKNOWN;

    companion object {
        fun fromApi(value: String?): TaskStatus = when (
            value?.trim()?.uppercase()
        ) {
            "PENDING" -> PENDING
            "IN_PROGRESS", "IN PROGRESS" -> IN_PROGRESS
            "COMPLETED" -> COMPLETED
            "DELIVERED" -> DELIVERED
            "CANCELLED", "CANCELED" -> CANCELLED
            else -> UNKNOWN
        }
    }

    fun toApiValue(): String = when (this) {
        PENDING -> "PENDING"
        IN_PROGRESS -> "IN_PROGRESS"
        COMPLETED -> "COMPLETED"
        DELIVERED -> "DELIVERED"
        CANCELLED -> "CANCELLED"
        UNKNOWN -> "PENDING"
    }
}
