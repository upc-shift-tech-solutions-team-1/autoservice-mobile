package com.torquelab.autoservice.mechanic.domain.model

data class MechanicTaskPart(
    val inventoryItemId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val purchasePrice: Double,
    val brand: String,
    val qualityTier: String
)
