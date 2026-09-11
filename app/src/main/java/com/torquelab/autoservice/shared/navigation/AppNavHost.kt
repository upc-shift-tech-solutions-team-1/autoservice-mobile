package com.torquelab.autoservice.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.torquelab.autoservice.R
import com.torquelab.autoservice.auth.presentation.login.LoginRoute
import com.torquelab.autoservice.auth.presentation.register.RegisterRoute
import com.torquelab.autoservice.shared.session.SessionManager

@Composable
fun AppNavHost(
    navController: NavHostController,
    sessionManager: SessionManager
) {

    fun navigateToAuthenticatedDestination() {

        val session =
            sessionManager.currentSession()
                ?: return

        val destination =
            AuthDestinationResolver.resolve(
                session
            )

        navController.navigate(
            destination
        ) {

            popUpTo(
                AppRoute.Login.route
            ) {
                inclusive = true
            }

            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination =
            AppRoute.Splash.route
    ) {

        composable(
            route =
                AppRoute.Splash.route
        ) {

            SplashScreen(
                sessionManager =
                    sessionManager,

                onAuthenticated = {
                        session ->

                    val destination =
                        AuthDestinationResolver.resolve(
                            session
                        )

                    navController.navigate(
                        destination
                    ) {

                        popUpTo(
                            AppRoute.Splash.route
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },

                onUnauthenticated = {

                    navController.navigate(
                        AppRoute.Login.route
                    ) {

                        popUpTo(
                            AppRoute.Splash.route
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route =
                AppRoute.Login.route
        ) {

            LoginRoute(

                onLoginSuccess = {

                    navigateToAuthenticatedDestination()
                },

                onRegisterClick = {

                    navController.navigate(
                        AppRoute.Register.route
                    )
                }
            )
        }

        composable(
            route =
                AppRoute.Register.route
        ) {

            RegisterRoute(

                onRegistrationSuccess = {

                    navigateToAuthenticatedDestination()
                },

                onSignInClick = {

                    navController.popBackStack()
                }
            )
        }

        composable(
            route =
                AppRoute.AdminHome.route
        ) {

            HomePlaceholderRoute(
                title =
                    stringResource(
                        R.string.admin_home_title
                    ),

                onLoggedOut = {

                    navController.navigate(
                        AppRoute.Login.route
                    ) {

                        popUpTo(
                            AppRoute.AdminHome.route
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route =
                AppRoute.MechanicHome.route
        ) {

            HomePlaceholderRoute(
                title =
                    stringResource(
                        R.string.mechanic_home_title
                    ),

                onLoggedOut = {

                    navController.navigate(
                        AppRoute.Login.route
                    ) {

                        popUpTo(
                            AppRoute.MechanicHome.route
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }
    }
}