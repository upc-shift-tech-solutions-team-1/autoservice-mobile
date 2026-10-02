package com.torquelab.autoservice.inventory.domain.repository

import com.torquelab.autoservice.inventory.domain.model.Product

interface InventoryRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductById(id: Int): Result<Product?>
    suspend fun saveProduct(product: Product): Result<Product>
}
