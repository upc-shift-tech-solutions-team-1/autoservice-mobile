package com.torquelab.autoservice.shared.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import com.torquelab.autoservice.R

object RoleNavigationItems {

    val admin = listOf(
        AuthenticatedNavigationItem(
            key = AuthenticatedSection.DASHBOARD,
            labelRes = R.string.nav_dashboard,
            icon = Icons.Outlined.Dashboard
        ),
        AuthenticatedNavigationItem(
            key = AuthenticatedSection.VEHICLES,
            labelRes = R.string.nav_vehicles,
            icon = Icons.Outlined.DirectionsCar
        ),
        AuthenticatedNavigationItem(
            key = AuthenticatedSection.WORK_ORDERS,
            labelRes = R.string.nav_work_orders,
            icon = Icons.Outlined.Assignment
        ),
        AuthenticatedNavigationItem(
            key = AuthenticatedSection.INVENTORY,
            labelRes = R.string.nav_inventory,
            icon = Icons.Outlined.Inventory2
        ),
        AuthenticatedNavigationItem(
            key = AuthenticatedSection.STAFF,
            labelRes = R.string.nav_staff,
            icon = Icons.Outlined.People
        )
    )

    val mechanic = listOf(
        AuthenticatedNavigationItem(
            key = AuthenticatedSection.WORKSPACE,
            labelRes = R.string.nav_workspace,
            icon = Icons.Outlined.Engineering
        )
    )
}
