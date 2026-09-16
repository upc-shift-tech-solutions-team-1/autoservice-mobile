package com.torquelab.autoservice.shared.navigation

sealed class AppRoute(
    val route: String
) {

    data object Splash : AppRoute("splash")

    data object Login : AppRoute("login")

    data object Register : AppRoute("register")

    data object AdminHome : AppRoute("admin_home")

    data object MechanicHome : AppRoute("mechanic_home")

    data object Tracking : AppRoute("tracking")

    data object Customers : AppRoute("customers")
}