package com.torquelab.autoservice.shared.management

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.fleet.domain.*
import com.torquelab.autoservice.inventory.domain.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ManagementRoute(isFleet: Boolean, demo: Boolean = false, viewModel: ManagementViewModel = hiltViewModel(key = if (isFleet) "fleet-management" else "inventory-management")) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(isFleet, demo) {
        viewModel.initialize(demo)
        viewModel.refresh(isFleet)
    }
    var vehicleForm by remember { mutableStateOf<Vehicle?>(null) }
    var itemForm by remember { mutableStateOf<InventoryItem?>(null) }
    var receiptForm by remember { mutableStateOf<InventoryItem?>(null) }
    var detail by remember { mutableStateOf<Vehicle?>(null) }
    var query by rememberSaveable(isFleet) { mutableStateOf("") }
    var filter by rememberSaveable(isFleet) { mutableStateOf("") }
    LaunchedEffect(state.savedVersion) {
        vehicleForm = null; itemForm = null; receiptForm = null
    }
    LaunchedEffect(isFleet) {
        vehicleForm = null; itemForm = null; receiptForm = null; detail = null
    }
    BackHandler(vehicleForm != null || itemForm != null || receiptForm != null || detail != null) {
        if (!state.saving) {
            vehicleForm = null; itemForm = null; receiptForm = null; detail = null
            viewModel.clearFeedback()
        }
    }
    fun close() { vehicleForm = null; itemForm = null; receiptForm = null; detail = null; viewModel.clearFeedback() }

    when {
        vehicleForm != null -> VehicleEditor(vehicleForm!!, state, viewModel::save, ::close)
        itemForm != null -> InventoryEditor(itemForm!!, state, viewModel::save, ::close)
        receiptForm != null -> ReceiptEditor(receiptForm!!, state, viewModel::receive, ::close)
        detail != null -> {
            val vehicle = detail!!
            EditorShell(R.string.m_vehicle_details, state, ::close) {
                Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.headlineSmall)
                Text(vehicle.plate, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.m_year) + ": " + vehicle.year)
                Text(stringResource(R.string.m_color) + ": " + vehicle.color)
                Text(stringResource(R.string.m_owner) + ": " + (state.owners.find { it.id == vehicle.customerId }?.fullName ?: "#${vehicle.customerId}"))
                Text(statusLabel(vehicle.status))
                Button(onClick = { detail = null; vehicleForm = vehicle }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.m_edit)) }
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text(stringResource(if (isFleet) R.string.nav_vehicles else R.string.nav_inventory), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(if (isFleet) R.string.m_fleet_subtitle else R.string.m_inventory_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            viewModel.clearFeedback()
                            if (isFleet) vehicleForm = Vehicle() else itemForm = InventoryItem()
                        }, enabled = !state.loading && !state.saving, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.m_add)) }
                        OutlinedButton(onClick = { viewModel.refresh(isFleet) }, enabled = !state.loading && !state.saving) { Text(stringResource(R.string.m_refresh)) }
                    }
                }
                item { Feedback(state) }
                item { OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.m_search)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    ChoiceField(stringResource(R.string.m_filter), filter,
                        if (isFleet) listOf("" to stringResource(R.string.m_all), "IN_WORKSHOP" to statusLabel("IN_WORKSHOP"), "READY" to statusLabel("READY"), "DELIVERED" to statusLabel("DELIVERED"))
                        else listOf("" to stringResource(R.string.m_all), "LOW" to stringResource(R.string.m_low_stock)),
                        { filter = it })
                }
                if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (isFleet) {
                    val vehicles = state.vehicles.filter { vehicle -> vehicle.matches(query, state.owners.find { it.id == vehicle.customerId }?.fullName.orEmpty()) && (filter.isEmpty() || vehicle.status == filter) }
                    if (vehicles.isEmpty() && !state.loading) item { EmptyResult() }
                    items(vehicles, key = { it.id }) { vehicle ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(vehicle.plate, style = MaterialTheme.typography.titleLarge)
                                Text("${vehicle.brand} ${vehicle.model} · ${vehicle.year}")
                                Text(state.owners.find { it.id == vehicle.customerId }?.fullName ?: "#${vehicle.customerId}")
                                Text(statusLabel(vehicle.status), color = MaterialTheme.colorScheme.primary)
                                Row {
                                    TextButton(onClick = { detail = vehicle }) { Text(stringResource(R.string.m_details)) }
                                    TextButton(onClick = { viewModel.clearFeedback(); vehicleForm = vehicle }) { Text(stringResource(R.string.m_edit)) }
                                }
                            }
                        }
                    }
                } else {
                    val inventory = state.items.filter { it.matches(query) && (filter.isEmpty() || it.lowStock) }
                    if (inventory.isEmpty() && !state.loading) item { EmptyResult() }
                    items(inventory, key = { it.id }) { item ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleLarge)
                                Text(item.brand)
                                Text(stringResource(R.string.m_stock_summary, item.stock, item.minStock))
                                Text(stringResource(if (item.stock == 0) R.string.m_no_stock else if (item.lowStock) R.string.m_low_stock else R.string.m_available),
                                    color = if (item.lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                Text(stringResource(R.string.m_sale_price) + ": " + money(item.unitPrice))
                                Text(stringResource(R.string.m_purchase_price) + ": " + money(item.purchasePrice))
                                Text(stringResource(R.string.m_profit) + ": " + money(item.profit) + " (${item.margin.toPlainString()}%)")
                                Row {
                                    TextButton(onClick = { viewModel.clearFeedback(); itemForm = item }) { Text(stringResource(R.string.m_edit)) }
                                    TextButton(onClick = { viewModel.clearFeedback(); receiptForm = item }) { Text(stringResource(R.string.m_receive)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun money(value: BigDecimal): String = NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(value)

@Composable private fun EmptyResult() { Text(stringResource(R.string.m_empty), modifier = Modifier.padding(vertical = 24.dp)) }

@Composable private fun Feedback(state: ManagementState) {
    state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
    state.message?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.primary) }
    if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable private fun statusLabel(value: String): String = when (value) {
    "IN_WORKSHOP" -> stringResource(R.string.m_in_workshop)
    "READY" -> stringResource(R.string.m_ready)
    "DELIVERED" -> stringResource(R.string.m_delivered)
    else -> value
}

@Composable private fun EditorShell(title: Int, state: ManagementState, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onClose, enabled = !state.saving) { Text(stringResource(R.string.m_back)) }
        Text(stringResource(title), style = MaterialTheme.typography.headlineSmall)
        Feedback(state)
        content()
    }
}

@Composable private fun Field(label: Int, value: String, onChange: (String) -> Unit, enabled: Boolean = true, numeric: Boolean = false) {
    OutlinedTextField(value, onChange, modifier = Modifier.fillMaxWidth(), enabled = enabled,
        label = { Text(stringResource(label)) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text))
}

@Composable private fun ChoiceField(label: String, value: String, choices: List<Pair<String, String>>, onChange: (String) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text(label + ": " + (choices.find { it.first == value }?.second ?: stringResource(R.string.m_select)))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { (key, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onChange(key); expanded = false }) }
        }
    }
}

@Composable private fun VehicleEditor(original: Vehicle, state: ManagementState, onSave: (Vehicle) -> Unit, onClose: () -> Unit) {
    var plate by rememberSaveable(original.id) { mutableStateOf(original.plate) }
    var brand by rememberSaveable(original.id) { mutableStateOf(original.brand) }
    var model by rememberSaveable(original.id) { mutableStateOf(original.model) }
    var year by rememberSaveable(original.id) { mutableStateOf(original.year) }
    var color by rememberSaveable(original.id) { mutableStateOf(original.color) }
    var status by rememberSaveable(original.id) { mutableStateOf(original.status) }
    var owner by rememberSaveable(original.id) { mutableStateOf(original.customerId.toString()) }
    EditorShell(R.string.m_vehicle_form, state, onClose) {
        Field(R.string.m_plate, plate, { plate = it }, !state.saving)
        Field(R.string.m_brand, brand, { brand = it }, !state.saving)
        Field(R.string.m_model, model, { model = it }, !state.saving)
        Field(R.string.m_year, year, { year = it }, !state.saving, true)
        Field(R.string.m_color, color, { color = it }, !state.saving)
        ChoiceField(stringResource(R.string.m_status), status, listOf("IN_WORKSHOP", "READY", "DELIVERED").map { it to statusLabel(it) }, { status = it }, !state.saving)
        ChoiceField(stringResource(R.string.m_owner), owner, state.owners.map { it.id.toString() to it.fullName }, { owner = it }, !state.saving)
        if (state.owners.isEmpty()) Text(stringResource(R.string.m_no_owners), color = MaterialTheme.colorScheme.error)
        Button(onClick = { onSave(original.copy(plate = plate, brand = brand, model = model, year = year, color = color, status = status, customerId = owner.toIntOrNull() ?: 0)) },
            enabled = !state.saving && !state.loading && state.owners.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.m_save)) }
    }
}

@Composable private fun InventoryEditor(original: InventoryItem, state: ManagementState, onSave: (InventoryItem) -> Unit, onClose: () -> Unit) {
    var name by rememberSaveable(original.id) { mutableStateOf(original.name) }
    var brand by rememberSaveable(original.id) { mutableStateOf(original.brand) }
    var category by rememberSaveable(original.id) { mutableStateOf(original.category) }
    var sale by rememberSaveable(original.id) { mutableStateOf(original.unitPrice.toPlainString()) }
    var purchase by rememberSaveable(original.id) { mutableStateOf(original.purchasePrice.toPlainString()) }
    var minimum by rememberSaveable(original.id) { mutableStateOf(original.minStock.toString()) }
    var specification by rememberSaveable(original.id) { mutableStateOf(original.specification) }
    var invalid by remember { mutableStateOf(false) }
    EditorShell(R.string.m_inventory_form, state, onClose) {
        Field(R.string.m_name, name, { name = it }, !state.saving)
        Field(R.string.m_brand, brand, { brand = it }, !state.saving)
        ChoiceField(stringResource(R.string.m_category), category, listOf("SPARE_PART" to stringResource(R.string.m_spare_part), "LUBRICANT" to stringResource(R.string.m_lubricant), "TOOL" to stringResource(R.string.m_tool)), { category = it }, !state.saving)
        Field(R.string.m_purchase_price, purchase, { purchase = it }, !state.saving, true)
        Field(R.string.m_sale_price, sale, { sale = it }, !state.saving, true)
        Field(R.string.m_min_stock, minimum, { minimum = it }, !state.saving, true)
        Field(R.string.m_specification, specification, { specification = it }, !state.saving)
        Text(stringResource(R.string.m_stock_note))
        if (invalid) Text(stringResource(R.string.m_item_invalid), color = MaterialTheme.colorScheme.error)
        Button(onClick = {
            val unitPrice = sale.replace(',', '.').toBigDecimalOrNull()
            val purchasePrice = purchase.replace(',', '.').toBigDecimalOrNull()
            val minStock = minimum.toIntOrNull()
            invalid = unitPrice == null || purchasePrice == null || minStock == null
            if (!invalid) onSave(original.copy(name = name, brand = brand, category = category, unitPrice = unitPrice!!, purchasePrice = purchasePrice!!, minStock = minStock!!, specification = specification))
        }, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.m_save)) }
    }
}

@Composable private fun ReceiptEditor(item: InventoryItem, state: ManagementState, onSave: (Int, StockReceipt) -> Unit, onClose: () -> Unit) {
    var quantity by rememberSaveable(item.id) { mutableStateOf("") }
    var provider by rememberSaveable(item.id) { mutableStateOf("") }
    var document by rememberSaveable(item.id) { mutableStateOf("") }
    var notes by rememberSaveable(item.id) { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val receipt = StockReceipt(quantity.toIntOrNull() ?: 0, provider.trim(), document.trim().ifEmpty { null }, notes.trim().ifEmpty { null })
    EditorShell(R.string.m_receive, state, onClose) {
        Text(item.name, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.m_stock_summary, item.stock, item.minStock))
        Field(R.string.m_quantity, quantity, { quantity = it }, !state.saving, true)
        Field(R.string.m_provider, provider, { provider = it }, !state.saving)
        Field(R.string.m_document, document, { document = it }, !state.saving)
        Field(R.string.m_notes, notes, { notes = it }, !state.saving)
        Text(stringResource(R.string.m_receipt_help))
        Button(onClick = { confirm = true }, enabled = receipt.isValid() && !state.saving, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.m_receive)) }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(stringResource(R.string.m_receive)) },
        text = { Text(stringResource(R.string.m_confirm_receipt, receipt.quantity, item.name)) },
        confirmButton = { TextButton(onClick = { confirm = false; onSave(item.id, receipt) }) { Text(stringResource(R.string.m_confirm)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.common_cancel)) } })
}
