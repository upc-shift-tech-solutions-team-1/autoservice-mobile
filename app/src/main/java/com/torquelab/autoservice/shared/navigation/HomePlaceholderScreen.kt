package com.torquelab.autoservice.shared.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.session.SessionViewModel
import com.torquelab.autoservice.ui.theme.AutoServiceTheme

@Composable
fun HomePlaceholderRoute(
    title: String,
    onLoggedOut: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {

    val uiState by
    viewModel.uiState.collectAsState()

    LaunchedEffect(
        uiState.isLoggedOut
    ) {

        if (uiState.isLoggedOut) {

            viewModel.consumeLogout()

            onLoggedOut()
        }
    }

    HomePlaceholderScreen(
        title = title,
        isLoggingOut =
            uiState.isLoggingOut,
        onLogoutClick =
            viewModel::logout
    )
}

@Composable
fun HomePlaceholderScreen(
    title: String,
    isLoggingOut: Boolean,
    onLogoutClick: () -> Unit
) {

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
                R.string.app_name
            ),
            style =
                MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = title,
            style =
                MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        OutlinedButton(
            onClick = onLogoutClick,
            modifier =
                Modifier.fillMaxWidth(),
            enabled =
                !isLoggingOut
        ) {

            if (isLoggingOut) {

                CircularProgressIndicator()

            } else {

                Text(
                    text = stringResource(
                        R.string.home_sign_out
                    )
                )
            }
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
            title = "Admin Home",
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
            isLoggingOut = false,
            onLogoutClick = {}
        )
    }
}