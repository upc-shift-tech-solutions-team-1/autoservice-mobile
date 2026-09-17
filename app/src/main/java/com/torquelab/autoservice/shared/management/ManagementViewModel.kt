package com.torquelab.autoservice.shared.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.torquelab.autoservice.R
import com.torquelab.autoservice.BuildConfig
import com.torquelab.autoservice.fleet.data.RemoteFleetRepository
import com.torquelab.autoservice.fleet.domain.*
import com.torquelab.autoservice.inventory.data.RemoteInventoryRepository
import com.torquelab.autoservice.inventory.domain.*
import com.torquelab.autoservice.shared.demo.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.Year

data class ManagementState(
    val vehicles: List<Vehicle> = emptyList(),
    val owners: List<VehicleOwner> = emptyList(),
    val items: List<InventoryItem> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: Int? = null,
    val message: Int? = null,
    val savedVersion: Int = 0
)

@HiltViewModel
class ManagementViewModel @Inject constructor(
    private val remoteFleet: RemoteFleetRepository,
    private val remoteInventory: RemoteInventoryRepository
) : ViewModel() {
    private var mode: Boolean? = null
    private lateinit var fleet: FleetRepository
    private lateinit var inventory: InventoryRepository
    private val mutable = MutableStateFlow(ManagementState())
    val state = mutable.asStateFlow()

    fun initialize(demo: Boolean) {
        if (mode != null) { check(mode == demo); return }
        check(!demo || BuildConfig.DEBUG)
        mode = demo
        fleet = if (demo) DemoFleetRepository() else remoteFleet
        inventory = if (demo) DemoInventoryRepository() else remoteInventory
    }

    fun clearFeedback() = mutable.update { it.copy(error = null, message = null) }

    fun refresh(isFleet: Boolean) {
        if (mutable.value.loading || mutable.value.saving) return
        mutable.update { it.copy(loading = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                if (isFleet) {
                    val vehicles = fleet.vehicles()
                    mutable.update { it.copy(vehicles = vehicles) }
                    val owners = fleet.owners()
                    mutable.update { it.copy(owners = owners) }
                } else {
                    val items = inventory.items()
                    mutable.update { it.copy(items = items) }
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { mutable.update { it.copy(error = errorFor(e)) }
            } finally { mutable.update { it.copy(loading = false) } }
        }
    }

    fun save(vehicle: Vehicle) {
        if (vehicle.validate(Year.now().value) != null || mutable.value.owners.none { it.id == vehicle.customerId }) {
            mutable.update { it.copy(error = R.string.m_vehicle_invalid) }; return
        }
        mutate {
            val saved = fleet.save(vehicle)
            mutable.update { it.copy(vehicles = it.vehicles.filterNot { old -> old.id == saved.id } + saved) }
        }
    }

    fun save(item: InventoryItem) {
        if (item.validate() != null) { mutable.update { it.copy(error = R.string.m_item_invalid) }; return }
        mutate {
            val saved = inventory.save(item)
            mutable.update { it.copy(items = it.items.filterNot { old -> old.id == saved.id } + saved) }
        }
    }

    fun receive(id: Int, receipt: StockReceipt) {
        if (!receipt.isValid()) { mutable.update { it.copy(error = R.string.m_receipt_invalid) }; return }
        mutate {
            val saved = inventory.receive(id, receipt)
            mutable.update { it.copy(items = it.items.map { old -> if (old.id == id) saved else old }) }
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        if (mutable.value.saving || mutable.value.loading) return
        mutable.update { it.copy(saving = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                block()
                mutable.update { it.copy(savedVersion = it.savedVersion + 1, message = R.string.m_saved) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { mutable.update { it.copy(error = errorFor(e)) }
            } finally { mutable.update { it.copy(saving = false) } }
        }
    }

    private fun errorFor(e: Exception): Int = when (e) {
        is IOException -> R.string.m_network_error
        is HttpException -> when (e.code()) {
            401, 403 -> R.string.m_access_error
            400, 409, 422 -> R.string.m_validation_error
            else -> R.string.m_server_error
        }
        else -> R.string.m_operation_error
    }
}
