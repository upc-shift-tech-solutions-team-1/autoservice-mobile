package com.torquelab.autoservice.inventory.data

import com.torquelab.autoservice.inventory.domain.*
import java.math.BigDecimal
import retrofit2.http.*
import retrofit2.Retrofit
import javax.inject.Inject

// Stock is deliberately absent: the server changes it only through receipts/consumption.
data class InventoryRequest(val name: String, val category: String, val brand: String,
    val unitPrice: BigDecimal, val minStock: Int, val image: String,
    val purchasePrice: BigDecimal, val qualityTier: String, val specification: String,
    val presentation: String, val unitMeasure: String)

fun InventoryItem.toRequest() = InventoryRequest(name.trim(), category, brand.trim(),
    unitPrice, minStock, image, purchasePrice, qualityTier, specification.trim(), presentation, unitMeasure)

data class ReceiptResponse(val item: InventoryItem)

interface InventoryApi {
    @GET("inventoryitems") suspend fun items(): List<InventoryItem>
    @POST("inventoryitems") suspend fun create(@Body body: InventoryRequest): InventoryItem
    @PUT("inventoryitems/{id}") suspend fun update(@Path("id") id: Int, @Body body: InventoryRequest): InventoryItem
    @POST("inventoryitems/{id}/receipts") suspend fun receive(@Path("id") id: Int, @Body body: StockReceipt): ReceiptResponse
}

class RemoteInventoryRepository @Inject constructor(retrofit: Retrofit) : InventoryRepository {
    private val api = retrofit.create(InventoryApi::class.java)
    override suspend fun items() = api.items()
    override suspend fun save(item: InventoryItem): InventoryItem =
        if (item.id == 0) api.create(item.toRequest()) else api.update(item.id, item.toRequest())
    override suspend fun receive(id: Int, receipt: StockReceipt) = api.receive(id, receipt).item
}
