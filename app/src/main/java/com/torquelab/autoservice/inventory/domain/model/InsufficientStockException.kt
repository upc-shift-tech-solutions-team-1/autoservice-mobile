package com.torquelab.autoservice.inventory.domain.model

class InsufficientStockException(
    val availableStock: Int,
    val requestedQuantity: Int,
    message: String = "Stock insuficiente. Disponible: $availableStock, Solicitado: $requestedQuantity"
) : Exception(message)
