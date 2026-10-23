package com.torquelab.autoservice.inventory.data.remote

import com.torquelab.autoservice.inventory.data.remote.dto.ProductDto

interface InventoryRemoteDataSource {
    suspend fun getProducts(): List<ProductDto>
    suspend fun getProductById(id: Int): ProductDto
    suspend fun saveProduct(productDto: ProductDto): ProductDto
}
