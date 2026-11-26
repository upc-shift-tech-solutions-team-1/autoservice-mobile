package com.torquelab.autoservice.inventory.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.torquelab.autoservice.R
import com.torquelab.autoservice.inventory.domain.InventoryItem
import com.torquelab.autoservice.inventory.domain.model.StockAvailability
import com.torquelab.autoservice.workshop.domain.model.WorkshopWorkOrder

@Composable
fun TaskPartsAvailabilityPanel(
    inventory: List<InventoryItem>,
    workOrders: List<WorkshopWorkOrder>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedKey by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }

    val taskOptions = workOrders.flatMap { order ->
        order.tasks
            .filter { it.parts.isNotEmpty() }
            .map { task -> order to task }
    }

    val selected = taskOptions.firstOrNull { (order, task) ->
        selectedKey == (order.id to task.id)
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.sa_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(stringResource(R.string.sa_description))

            if (taskOptions.isEmpty()) {
                Text(stringResource(R.string.sa_no_tasks))
            } else {
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selected?.let { (order, task) ->
                                stringResource(
                                    R.string.sa_order_task,
                                    order.id,
                                    task.id,
                                    task.description
                                )
                            } ?: stringResource(R.string.sa_select_task)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        taskOptions.forEach { (order, task) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.sa_order_task,
                                            order.id,
                                            task.id,
                                            task.description
                                        )
                                    )
                                },
                                onClick = {
                                    selectedKey = order.id to task.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                selected?.let { (_, task) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Group repeated entries for the same inventory item
                        // so the comparison uses the total task requirement.
                        task.parts
                            .groupBy { it.inventoryItemId }
                            .forEach { (itemId, parts) ->
                                val item = inventory.singleOrNull {
                                    it.id == itemId
                                }

                                val totalRequired = parts.sumOf {
                                    it.quantity.toLong()
                                }

                                val validRequirement =
                                    parts.all { it.quantity > 0 } &&
                                            totalRequired in
                                            1L..Int.MAX_VALUE.toLong()

                                val availability =
                                    if (
                                        validRequirement &&
                                        item != null &&
                                        item.stock >= 0
                                    ) {
                                        StockAvailability(
                                            availableQuantity = item.stock,
                                            requiredQuantity =
                                                totalRequired.toInt()
                                        )
                                    } else {
                                        null
                                    }

                                HorizontalDivider()

                                Text(
                                    text = item?.name
                                        ?: parts.first().name,
                                    style =
                                        MaterialTheme.typography.titleSmall
                                )

                                if (validRequirement) {
                                    Text(
                                        stringResource(
                                            R.string.sa_required,
                                            totalRequired.toInt()
                                        )
                                    )
                                }

                                if (item != null && item.stock >= 0) {
                                    Text(
                                        stringResource(
                                            R.string.sa_available,
                                            item.stock
                                        )
                                    )
                                }

                                val result = when {
                                    !validRequirement ->
                                        stringResource(
                                            R.string.sa_invalid_quantity
                                        )

                                    item == null ->
                                        stringResource(
                                            R.string.sa_unknown_stock
                                        )

                                    item.stock < 0 ->
                                        stringResource(
                                            R.string.sa_invalid_stock
                                        )

                                    availability?.isSufficient == true ->
                                        stringResource(
                                            R.string.sa_sufficient
                                        )

                                    else ->
                                        stringResource(
                                            R.string.sa_insufficient,
                                            availability!!.missingQuantity
                                        )
                                }

                                Text(
                                    text = result,
                                    color =
                                        if (
                                            availability?.isSufficient == true
                                        ) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                )
                            }
                    }
                }
            }

            Text(
                text = stringResource(R.string.sa_read_only),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.sa_refresh_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}