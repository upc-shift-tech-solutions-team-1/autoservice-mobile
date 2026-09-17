package com.torquelab.autoservice.shared.navigation

import com.torquelab.autoservice.shared.session.UserRole
import com.torquelab.autoservice.shared.session.UserSession

object AuthDestinationResolver {

    fun resolve(
        session: UserSession
    ): String {

        return when (
            session.role.lowercase()
        ) {

            UserRole.MECHANIC ->
                AppRoute.MechanicHome.route

            else ->
                AppRoute.AdminHome.route
        }
    }
}