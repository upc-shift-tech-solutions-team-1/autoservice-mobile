package com.torquelab.autoservice.auth.presentation.register

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun RegisterRoute(
    onRegistrationSuccess: () -> Unit,
    onSignInClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(
        uiState.isRegistrationSuccessful
    ) {
        if (uiState.isRegistrationSuccessful) {
            viewModel.consumeRegistrationSuccess()
            onRegistrationSuccess()
        }
    }

    RegisterScreen(
        uiState = uiState,
        onWorkshopNameChange =
            viewModel::onWorkshopNameChange,
        onEmailChange =
            viewModel::onEmailChange,
        onPasswordChange =
            viewModel::onPasswordChange,
        onConfirmPasswordChange =
            viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility =
            viewModel::togglePasswordVisibility,
        onToggleConfirmPasswordVisibility =
            viewModel::toggleConfirmPasswordVisibility,
        onRegister =
            viewModel::registerWorkshop,
        onSignInClick =
            onSignInClick
    )
}

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onWorkshopNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegister: () -> Unit,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(
                R.string.register_title
            ),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = stringResource(
                R.string.register_description
            ),
            style = MaterialTheme.typography.bodyLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        AutoServiceTextField(
            value = uiState.workshopName,
            onValueChange =
                onWorkshopNameChange,
            label = stringResource(
                R.string.register_workshop_name
            ),
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Next
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        AutoServiceTextField(
            value = uiState.email,
            onValueChange =
                onEmailChange,
            label = stringResource(
                R.string.register_email
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
            onValueChange =
                onPasswordChange,
            label = stringResource(
                R.string.register_password
            ),
            isPasswordVisible =
                uiState.isPasswordVisible,
            onTogglePasswordVisibility =
                onTogglePasswordVisibility,
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Next
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        AutoServicePasswordField(
            value =
                uiState.confirmPassword,
            onValueChange =
                onConfirmPasswordChange,
            label = stringResource(
                R.string.register_confirm_password
            ),
            isPasswordVisible =
                uiState.isConfirmPasswordVisible,
            onTogglePasswordVisibility =
                onToggleConfirmPasswordVisibility,
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Done,
            onDone = {
                if (!uiState.isLoading) {
                    onRegister()
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
                R.string.register_create_workshop
            ),
            onClick = onRegister,
            isLoading = uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        TextButton(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = stringResource(
                    R.string.register_already_account
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
private fun RegisterScreenPreview() {

    AutoServiceTheme {

        RegisterScreen(
            uiState = RegisterUiState(
                workshopName = "Torque Garage",
                email = "admin@torquegarage.com"
            ),
            onWorkshopNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onRegister = {},
            onSignInClick = {}
        )
    }
}