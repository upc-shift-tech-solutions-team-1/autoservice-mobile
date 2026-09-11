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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.torquelab.autoservice.R
import com.torquelab.autoservice.ui.theme.AutoServiceTheme

@Composable
fun RegisterRoute(
    onRegistrationSuccess: () -> Unit,
    onSignInClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRegistrationSuccessful) {
        if (uiState.isRegistrationSuccessful) {
            viewModel.consumeRegistrationSuccess()
            onRegistrationSuccess()
        }
    }

    RegisterScreen(
        uiState = uiState,
        onWorkshopNameChange = viewModel::onWorkshopNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onToggleConfirmPasswordVisibility =
            viewModel::toggleConfirmPasswordVisibility,
        onRegister = viewModel::registerWorkshop,
        onSignInClick = onSignInClick
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
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        OutlinedTextField(
            value = uiState.workshopName,
            onValueChange = onWorkshopNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(
                        R.string.register_workshop_name
                    )
                )
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(
                        R.string.register_email
                    )
                )
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(
                        R.string.register_password
                    )
                )
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation =
                if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {
                TextButton(
                    onClick = onTogglePasswordVisibility
                ) {
                    Text(
                        text =
                            if (uiState.isPasswordVisible) {
                                stringResource(
                                    R.string.common_hide
                                )
                            } else {
                                stringResource(
                                    R.string.common_show
                                )
                            }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    stringResource(
                        R.string.register_confirm_password
                    )
                )
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation =
                if (uiState.isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {
                TextButton(
                    onClick =
                        onToggleConfirmPasswordVisibility
                ) {
                    Text(
                        text =
                            if (
                                uiState.isConfirmPasswordVisible
                            ) {
                                stringResource(
                                    R.string.common_hide
                                )
                            } else {
                                stringResource(
                                    R.string.common_show
                                )
                            }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!uiState.isLoading) {
                        onRegister()
                    }
                }
            )
        )

        if (uiState.errorMessage != null) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {

            if (uiState.isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text(
                    text = stringResource(
                        R.string.register_create_workshop
                    )
                )
            }
        }

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