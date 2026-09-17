package com.torquelab.autoservice.shared.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.torquelab.autoservice.R

@Composable
fun AutoServicePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(text = label)
        },
        singleLine = true,
        enabled = enabled,
        visualTransformation =
            if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        trailingIcon = {
            TextButton(
                onClick = onTogglePasswordVisibility,
                enabled = enabled
            ) {
                Text(
                    text =
                        if (isPasswordVisible) {
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
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone?.invoke()
            }
        )
    )
}