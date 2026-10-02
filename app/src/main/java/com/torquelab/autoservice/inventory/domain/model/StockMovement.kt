package com.torquelab.autoservice.inventory.domain.model

enum class MovementType {
    IN,          // Restock / Reabastecimiento
    OUT,         // Service order exit / Salida por orden de servicio
    ADJUSTMENT   // Stock adjustment / Ajuste
}

data class StockMovement(
    val id: Int = 0,
    val productId: Int,
    val quantity: Int,
    val type: MovementType,
    val reason: String = "",
    val referenceDocument: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
