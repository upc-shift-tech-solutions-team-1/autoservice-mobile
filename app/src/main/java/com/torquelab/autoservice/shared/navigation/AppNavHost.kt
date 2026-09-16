package com.torquelab.autoservice.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.torquelab.autoservice.R
import com.torquelab.autoservice.auth.presentation.login.LoginRoute
import com.torquelab.autoservice.auth.presentation.register.RegisterRoute
import com.torquelab.autoservice.shared.session.SessionEvent
import com.torquelab.autoservice.shared.session.SessionEventManager
import com.torquelab.autoservice.shared.session.SessionManager
import com.torquelab.autoservice.shared.ui.navigation.AuthenticatedSection
import com.torquelab.autoservice.shared.ui.navigation.RoleNavigationItems

@Composable
fun AppNavHost(
    navController: NavHostController,
    sessionManager: SessionManager,
    sessionEventManager: SessionEventManager
) {

    /*
     * Global session events.
     *
     * If an authenticated API request receives a 401,
     * AuthInterceptor emits SessionExpired.
     *
     * We clear the local session and redirect the user
     * back to Login.
     */
    LaunchedEffect(sessionEventManager) {

        sessionEventManager.events.collect { event ->

            when (event) {

                SessionEvent.SessionExpired -> {

                    sessionManager.clearSession()

                    navController.navigate(
                        AppRoute.Login.route
                    ) {

                        popUpTo(
                            navController.graph.id
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            }
        }
    }

    /*
     * Used after Sign In or Register Workshop.
     *
     * AuthRepository already saved the session,
     * so we resolve the destination according
     * to the authenticated user's role.
     */
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
        startDestination = AppRoute.Splash.route
    ) {

        /*
         * SPLASH
         *
         * Restores the persisted session from DataStore.
         */
        composable(
            route = AppRoute.Splash.route
        ) {

            SplashScreen(
                sessionManager = sessionManager,

                onAuthenticated = { session ->

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

        /*
         * LOGIN
         */
        composable(
            route = AppRoute.Login.route
        ) {

            LoginRoute(
                onDemoClick = if (com.torquelab.autoservice.BuildConfig.DEBUG) {
                    { navController.navigate("fleet-inventory-demo") }
                } else null,

                onLoginSuccess = {

                    navigateToAuthenticatedDestination()
                },

                onRegisterClick = {

                    navController.navigate(
                        AppRoute.Register.route
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        if (com.torquelab.autoservice.BuildConfig.DEBUG) {
            composable("fleet-inventory-demo") {
                com.torquelab.autoservice.shared.demo.DemoScreen(onExit = { navController.popBackStack() })
            }
        }

        /*
         * REGISTER WORKSHOP
         */
        composable(
            route = AppRoute.Register.route
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

        /*
         * ADMIN AUTHENTICATED AREA
         */
        composable(
            route = AppRoute.AdminHome.route
        ) {

            HomePlaceholderRoute(
                title = stringResource(
                    R.string.admin_home_title
                ),

                navigationItems =
                    RoleNavigationItems.admin,

                initialDestinationKey =
                    AuthenticatedSection.DASHBOARD,

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

        /*
         * MECHANIC AUTHENTICATED AREA
         */
        composable(
            route = AppRoute.MechanicHome.route
        ) {

            HomePlaceholderRoute(
                title = stringResource(
                    R.string.mechanic_home_title
                ),

                navigationItems =
                    RoleNavigationItems.mechanic,

                initialDestinationKey =
                    AuthenticatedSection.WORKSPACE,

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
