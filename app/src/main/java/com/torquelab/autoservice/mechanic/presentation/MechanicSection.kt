package com.torquelab.autoservice.mechanic.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.torquelab.autoservice.mechanic.presentation.order_execution.MechanicOrderExecutionRoute
import com.torquelab.autoservice.mechanic.presentation.workspace.MechanicDashboardRoute

sealed class MechanicRoute(val route: String) {
    data object Dashboard : MechanicRoute("mechanic_dashboard")
    data object OrderExecution : MechanicRoute("mechanic_order_execution/{orderId}") {
        fun createRoute(orderId: Int) = "mechanic_order_execution/$orderId"
    }
}

@Composable
fun MechanicSection() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MechanicRoute.Dashboard.route
    ) {
        composable(MechanicRoute.Dashboard.route) {
            MechanicDashboardRoute(
                onOrderClick = { orderId ->
                    navController.navigate(MechanicRoute.OrderExecution.createRoute(orderId))
                }
            )
        }

        composable(
            route = MechanicRoute.OrderExecution.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) {
            MechanicOrderExecutionRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
