package com.torquelab.autoservice.inventory.data.repository

import com.torquelab.autoservice.inventory.data.remote.InventoryRemoteDataSource
import com.torquelab.autoservice.inventory.data.remote.dto.ProductDto
import com.torquelab.autoservice.inventory.domain.model.Product
import com.torquelab.autoservice.inventory.domain.repository.InventoryRepository
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: InventoryRemoteDataSource
) : InventoryRepository {

    override suspend fun getProducts(): Result<List<Product>> = runCatching {
        remoteDataSource.getProducts().map { it.toDomain() }
    }

    override suspend fun getProductById(id: Int): Result<Product?> = runCatching {
        remoteDataSource.getProductById(id).toDomain()
    }

    override suspend fun saveProduct(product: Product): Result<Product> = runCatching {
        val dto = ProductDto.fromDomain(product)
        remoteDataSource.saveProduct(dto).toDomain()
    }
}
