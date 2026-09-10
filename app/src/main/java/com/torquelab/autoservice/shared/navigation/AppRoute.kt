package com.torquelab.autoservice.shared.navigation

sealed class AppRoute(val route: String) {

    data object Splash : AppRoute("splash")

    data object Login : AppRoute("login")

    data object Home : AppRoute("home")
}