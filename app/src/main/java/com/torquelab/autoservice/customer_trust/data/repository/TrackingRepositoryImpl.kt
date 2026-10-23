package com.torquelab.autoservice.customer_trust.data.repository

import com.torquelab.autoservice.customer_trust.data.remote.TrackingApi
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrder
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderNotFoundException
import com.torquelab.autoservice.customer_trust.domain.model.TrackingOrderStatus
import com.torquelab.autoservice.customer_trust.domain.repository.TrackingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import javax.inject.Inject

class TrackingRepositoryImpl @Inject constructor(
    private val api: TrackingApi
) : TrackingRepository {
    override suspend fun getOrderByCode(code: String): Result<TrackingOrder> = runCatching {
        val workOrders = try {
            api.getOrderByCode(code)
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw TrackingOrderNotFoundException()
            throw exception
        }
        val workOrder = workOrders.firstOrNull()
            ?: throw TrackingOrderNotFoundException()

        coroutineScope {
            val vehicleRequest = async {
                fetchOptional { api.getVehicle(workOrder.vehicleId) }
            }
            val tasksRequest = async {
                fetchOptional { api.getTasksByOrder(workOrder.id) }.orEmpty()
            }
            val customerRequest = async {
                fetchOptional { api.getCustomer(workOrder.customerId) }
            }
            val workshopRequest = async {
                workOrder.workshopId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { id -> fetchOptional { api.getWorkshop(id) } }
            }

            val vehicle = vehicleRequest.await()
            val tasks = tasksRequest.await().map { it.toDomain() }
            val customer = customerRequest.await()
            val workshop = workshopRequest.await()

            TrackingOrder(
                trackingCode = workOrder.trackingCode.orEmpty(),
                vehicleBrand = vehicle?.brand,
                vehicleModel = vehicle?.model,
                vehiclePlate = vehicle?.plate,
                customerName = customer?.fullName,
                workshopName = workshop?.name,
                serviceDescription = workOrder.description,
                status = TrackingOrderStatus.fromApi(workOrder.status),
                tasks = tasks,
                estimatedDelivery = workOrder.estimatedDate,
                totalCost = workOrder.price
            )
        }
    }

    private suspend fun <T> fetchOptional(request: suspend () -> T): T? = try {
        request()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }
}
