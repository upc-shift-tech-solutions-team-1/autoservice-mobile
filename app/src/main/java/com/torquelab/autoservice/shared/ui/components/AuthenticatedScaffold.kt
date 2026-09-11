package com.torquelab.autoservice.shared.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.ui.navigation.AuthenticatedNavigationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedScaffold(
    title: String,
    navigationItems: List<AuthenticatedNavigationItem>,
    selectedDestinationKey: String,
    onDestinationSelected: (String) -> Unit,
    onSignOut: () -> Unit,
    isLoggingOut: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = title
                    )
                },
                actions = {

                    TextButton(
                        onClick = onSignOut,
                        enabled = !isLoggingOut
                    ) {

                        if (isLoggingOut) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                text = stringResource(
                                    R.string.home_sign_out
                                )
                            )
                        }
                    }
                }
            )
        },

        bottomBar = {

            NavigationBar {

                navigationItems.forEach { item ->

                    val label =
                        stringResource(
                            item.labelRes
                        )

                    NavigationBarItem(
                        selected =
                            selectedDestinationKey == item.key,

                        onClick = {
                            onDestinationSelected(
                                item.key
                            )
                        },

                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label
                            )
                        },

                        label = {
                            Text(
                                text = label
                            )
                        }
                    )
                }
            }
        },

        content = content
    )
}