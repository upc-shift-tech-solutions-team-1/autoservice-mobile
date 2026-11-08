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
import com.torquelab.autoservice.shared.network.apiResult

class TrackingRepositoryImpl @Inject constructor(
    private val api: TrackingApi
) : TrackingRepository {
    override suspend fun getOrderByCode(code: String): Result<TrackingOrder> = apiResult {
        val summary = try {
            api.getSummary(code.trim())
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw TrackingOrderNotFoundException()
            throw exception
        }
        // The public summary owns the tasks, total and progress; metadata is optional.
        val workOrder = fetchOptional { api.getOrderByCode(code.trim()).firstOrNull() }

        coroutineScope {
            val vehicleRequest = async {
                workOrder?.let { fetchOptional { api.getVehicle(it.vehicleId) } }
            }
            val customerRequest = async {
                workOrder?.let { fetchOptional { api.getCustomer(it.customerId) } }
            }
            val workshopRequest = async {
                workOrder?.workshopId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { id -> fetchOptional { api.getWorkshop(id) } }
            }

            val vehicle = vehicleRequest.await()
            // Public tasks have no database ID. These keys are local to the rendered list.
            val tasks = summary.tasks.orEmpty().mapIndexed { index, task -> task.toDomain().copy(id = index + 1) }
            val customer = customerRequest.await()
            val workshop = workshopRequest.await()

            TrackingOrder(
                trackingCode = summary.trackingCode,
                vehicleBrand = vehicle?.brand,
                vehicleModel = vehicle?.model,
                vehiclePlate = vehicle?.plate,
                customerName = customer?.fullName,
                workshopName = workshop?.name,
                serviceDescription = null,
                status = TrackingOrderStatus.fromApi(summary.status),
                tasks = tasks,
                estimatedDelivery = summary.estimatedDate,
                totalCost = summary.costs.total,
                reportedProgress = summary.progressPercentage.toInt(),
                costs = summary.costs,
                history = summary.history.orEmpty(),
                estimatedDateHistory = summary.estimatedDateHistory.orEmpty(),
                primaryMechanic = summary.primaryMechanic
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
