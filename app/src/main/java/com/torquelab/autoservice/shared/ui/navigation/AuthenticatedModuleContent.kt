package com.torquelab.autoservice.shared.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState

@Composable
fun AuthenticatedModuleContent(
    destinationKey: String
) {
    when (destinationKey) {

        AuthenticatedSection.DASHBOARD -> {
            ModulePlaceholder(
                titleRes = R.string.nav_dashboard
            )
        }

        AuthenticatedSection.VEHICLES -> {
            ModulePlaceholder(
                titleRes = R.string.nav_vehicles
            )
        }

        AuthenticatedSection.WORK_ORDERS -> {
            ModulePlaceholder(
                titleRes = R.string.nav_work_orders
            )
        }

        AuthenticatedSection.INVENTORY -> {
            ModulePlaceholder(
                titleRes = R.string.nav_inventory
            )
        }

        AuthenticatedSection.WORKSPACE -> {
            ModulePlaceholder(
                titleRes = R.string.nav_workspace
            )
        }

        else -> {
            ModulePlaceholder(
                titleRes = R.string.nav_dashboard
            )
        }
    }
}

@Composable
private fun ModulePlaceholder(
    @StringRes titleRes: Int
) {
    val moduleName =
        stringResource(titleRes)

    AutoServiceEmptyState(
        title = moduleName,
        description = stringResource(
            R.string.placeholder_module_description
        )
    )
}