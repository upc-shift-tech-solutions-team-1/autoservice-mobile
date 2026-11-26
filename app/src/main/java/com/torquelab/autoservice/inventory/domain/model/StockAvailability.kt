package com.torquelab.autoservice.inventory.domain.model

/**
 * Compares available stock with the quantity needed for a repair task.
 * This calculation does not reserve or consume inventory.
 */
data class StockAvailability(
    val availableQuantity: Int,
    val requiredQuantity: Int
) {
    init {
        require(availableQuantity >= 0) {
            "Available quantity cannot be negative."
        }
        require(requiredQuantity > 0) {
            "Required quantity must be greater than zero."
        }
    }

    val isSufficient: Boolean
        get() = availableQuantity >= requiredQuantity

    val missingQuantity: Int
        get() = if (isSufficient) {
            0
        } else {
            requiredQuantity - availableQuantity
        }
}