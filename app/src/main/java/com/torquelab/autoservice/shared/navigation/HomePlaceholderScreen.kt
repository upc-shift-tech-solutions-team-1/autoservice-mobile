package com.torquelab.autoservice.shared.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.session.SessionViewModel
import com.torquelab.autoservice.shared.session.UserRole
import com.torquelab.autoservice.shared.ui.components.AuthenticatedScaffold
import com.torquelab.autoservice.shared.ui.components.AutoServiceConfirmationDialog
import com.torquelab.autoservice.shared.ui.navigation.AuthenticatedModuleContent
import com.torquelab.autoservice.shared.ui.navigation.AuthenticatedNavigationItem
import com.torquelab.autoservice.shared.ui.navigation.AuthenticatedSection
import com.torquelab.autoservice.shared.ui.navigation.RoleNavigationItems
import com.torquelab.autoservice.ui.theme.AutoServiceTheme

@Composable
fun HomePlaceholderRoute(
    title: String,
    navigationItems: List<AuthenticatedNavigationItem>,
    initialDestinationKey: String,
    onLoggedOut: () -> Unit,
    role: String = UserRole.ADMIN,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedDestinationKey by rememberSaveable {
        mutableStateOf(initialDestinationKey)
    }

    var showLogoutDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.consumeLogout()
            onLoggedOut()
        }
    }

    HomePlaceholderScreen(
        title = title,
        navigationItems = navigationItems,
        selectedDestinationKey = selectedDestinationKey,
        role = role,

        onDestinationSelected = { destinationKey ->
            selectedDestinationKey = destinationKey
        },

        isLoggingOut = uiState.isLoggingOut,

        onLogoutClick = {
            showLogoutDialog = true
        }
    )

    if (showLogoutDialog) {

        AutoServiceConfirmationDialog(
            title = stringResource(
                R.string.logout_dialog_title
            ),

            message = stringResource(
                R.string.logout_dialog_message
            ),

            confirmText = stringResource(
                R.string.logout_dialog_confirm
            ),

            dismissText = stringResource(
                R.string.common_cancel
            ),

            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },

            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
fun HomePlaceholderScreen(
    title: String,
    navigationItems: List<AuthenticatedNavigationItem>,
    selectedDestinationKey: String,
    onDestinationSelected: (String) -> Unit,
    isLoggingOut: Boolean,
    onLogoutClick: () -> Unit,
    role: String = UserRole.ADMIN
) {
    AuthenticatedScaffold(
        title = title,
        navigationItems = navigationItems,
        selectedDestinationKey = selectedDestinationKey,
        onDestinationSelected = onDestinationSelected,
        onSignOut = onLogoutClick,
        isLoggingOut = isLoggingOut
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AuthenticatedModuleContent(
                destinationKey = selectedDestinationKey,
                role = role
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun AdminHomePlaceholderPreview() {

    AutoServiceTheme {

        HomePlaceholderScreen(
            title = "Admin Dashboard",
            navigationItems =
                RoleNavigationItems.admin,
            selectedDestinationKey =
                AuthenticatedSection.DASHBOARD,
            onDestinationSelected = {},
            isLoggingOut = false,
            onLogoutClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun MechanicHomePlaceholderPreview() {

    AutoServiceTheme {

        HomePlaceholderScreen(
            title = "Mechanic Workspace",
            navigationItems =
                RoleNavigationItems.mechanic,
            selectedDestinationKey =
                AuthenticatedSection.WORKSPACE,
            onDestinationSelected = {},
            isLoggingOut = false,
            onLogoutClick = {}
        )
    }
}
