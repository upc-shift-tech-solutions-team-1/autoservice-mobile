package com.torquelab.autoservice.inventory.di

import com.torquelab.autoservice.inventory.data.InventoryApi
import com.torquelab.autoservice.inventory.data.RemoteInventoryRepository
import com.torquelab.autoservice.inventory.data.remote.InventoryRemoteDataSource
import com.torquelab.autoservice.inventory.data.remote.dto.ProductDto
import com.torquelab.autoservice.inventory.data.repository.InventoryRepositoryImpl
import com.torquelab.autoservice.inventory.domain.InventoryRepository as LegacyInventoryRepository
import com.torquelab.autoservice.inventory.domain.repository.InventoryRepository
import com.torquelab.autoservice.inventory.domain.usecase.UpdateStockUseCase
import com.torquelab.autoservice.inventory.domain.usecase.UpdateStockUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InventoryModule {

    @Binds
    @Singleton
    abstract fun bindLegacyInventoryRepository(
        impl: RemoteInventoryRepository
    ): LegacyInventoryRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        impl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindUpdateStockUseCase(
        impl: UpdateStockUseCaseImpl
    ): UpdateStockUseCase

    companion object {
        @Provides
        @Singleton
        fun provideInventoryRemoteDataSource(api: InventoryApi): InventoryRemoteDataSource =
            object : InventoryRemoteDataSource {
                override suspend fun getProducts(): List<ProductDto> {
                    return api.items().map { item ->
                        ProductDto.fromDomain(item.toProduct())
                    }
                }

                override suspend fun getProductById(id: Int): ProductDto {
                    val item = api.items().first { it.id == id }
                    return ProductDto.fromDomain(item.toProduct())
                }

                override suspend fun saveProduct(productDto: ProductDto): ProductDto {
                    return productDto
                }
            }
    }
}
