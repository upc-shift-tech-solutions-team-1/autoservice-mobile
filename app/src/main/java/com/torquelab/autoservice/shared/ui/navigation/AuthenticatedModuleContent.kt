package com.torquelab.autoservice.shared.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.torquelab.autoservice.R

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
    val title =
        stringResource(titleRes)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = stringResource(
                R.string.placeholder_module,
                title
            ),
            style =
                MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = stringResource(
                R.string.placeholder_module_description
            ),
            style =
                MaterialTheme.typography.bodyLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}