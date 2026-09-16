package com.torquelab.autoservice.shared.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.torquelab.autoservice.R
import com.torquelab.autoservice.customer_management.presentation.CustomerListRoute
import com.torquelab.autoservice.shared.session.UserRole
import com.torquelab.autoservice.shared.ui.components.AutoServiceEmptyState
import com.torquelab.autoservice.staff.presentation.StaffRoute
import com.torquelab.autoservice.workshop.presentation.DashboardRoute
import com.torquelab.autoservice.workshop.presentation.WorkshopRoute

@Composable
fun AuthenticatedModuleContent(
    destinationKey: String,
    onNavigateToSection: (String) -> Unit,
    role: String = UserRole.ADMIN
) {
    when (destinationKey) {

        AuthenticatedSection.WORK_ORDERS -> {
            if (role.lowercase() == UserRole.ADMIN) {
                WorkshopRoute()
            } else {
                ModulePlaceholder(R.string.nav_work_orders)
            }
        }

        AuthenticatedSection.STAFF -> {
            if (role.lowercase() == UserRole.ADMIN) {
                StaffRoute()
            } else {
                ModulePlaceholder(R.string.nav_staff)
            }
        }

        AuthenticatedSection.DASHBOARD -> {
            DashboardRoute(
                onCustomersClick = {
                    onNavigateToSection(AuthenticatedSection.CUSTOMERS)
                }
            )
        }

        AuthenticatedSection.CUSTOMERS -> {
            CustomerListRoute(
                onBack = {
                    onNavigateToSection(AuthenticatedSection.DASHBOARD)
                }
            )
        }

        AuthenticatedSection.VEHICLES -> {
            ModulePlaceholder(
                titleRes = R.string.nav_vehicles
            )
        }

        AuthenticatedSection.INVENTORY -> {
            ModulePlaceholder(
                titleRes = R.string.nav_inventory
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
