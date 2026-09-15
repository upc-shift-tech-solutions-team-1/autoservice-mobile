package com.torquelab.autoservice.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.auth.presentation.stringResourceId
import com.torquelab.autoservice.shared.ui.components.AutoServiceErrorText
import com.torquelab.autoservice.shared.ui.components.AutoServicePasswordField
import com.torquelab.autoservice.shared.ui.components.AutoServicePrimaryButton
import com.torquelab.autoservice.shared.ui.components.AutoServiceTextField
import com.torquelab.autoservice.ui.theme.AutoServiceTheme

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            viewModel.consumeLoginSuccess()
            onLoginSuccess()
        }
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility =
            viewModel::togglePasswordVisibility,
        onSignIn = viewModel::signIn,
        onRegisterClick = onRegisterClick
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignIn: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(
                R.string.login_title
            ),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = stringResource(
                R.string.login_welcome_back
            ),
            style = MaterialTheme.typography.titleMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        AutoServiceTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = stringResource(
                R.string.login_email
            ),
            enabled = !uiState.isLoading,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        AutoServicePasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = stringResource(
                R.string.login_password
            ),
            isPasswordVisible =
                uiState.isPasswordVisible,
            onTogglePasswordVisibility =
                onTogglePasswordVisibility,
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Done,
            onDone = {
                if (!uiState.isLoading) {
                    onSignIn()
                }
            }
        )

        uiState.error?.let { error ->

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            AutoServiceErrorText(
                text = stringResource(
                    error.stringResourceId()
                )
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        AutoServicePrimaryButton(
            text = stringResource(
                R.string.login_sign_in
            ),
            onClick = onSignIn,
            isLoading = uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = stringResource(
                    R.string.login_create_workshop
                )
            )
        }

    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun LoginScreenPreview() {

    AutoServiceTheme {

        LoginScreen(
            uiState = LoginUiState(
                email = "admin@autoservice.com",
                password = "password"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onSignIn = {},
            onRegisterClick = {}
        )
    }
}
