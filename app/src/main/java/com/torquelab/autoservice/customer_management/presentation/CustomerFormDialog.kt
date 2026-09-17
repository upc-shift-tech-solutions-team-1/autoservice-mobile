package com.torquelab.autoservice.customer_management.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.torquelab.autoservice.R
import com.torquelab.autoservice.customer_management.domain.model.Customer
import com.torquelab.autoservice.shared.ui.components.AutoServiceTextField

@Composable
fun CustomerFormDialog(
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, dni: String, phone: String, email: String) -> Unit
) {
    var fullName by remember { mutableStateOf(customer?.fullName ?: "") }
    var dni by remember { mutableStateOf(customer?.dni ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }

    val isFormValid = fullName.isNotBlank() && dni.isNotBlank()
    val primaryBlue = Color(0xFF1E3A8A)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (customer == null) {
                    stringResource(R.string.customers_add_customer)
                } else {
                    stringResource(R.string.common_edit)
                }
            )
        },
        text = {
            Column {
                AutoServiceTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name *"
                )
                Spacer(modifier = Modifier.height(8.dp))
                AutoServiceTextField(
                    value = dni,
                    onValueChange = { dni = it },
                    label = "ID / DNI *",
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))
                AutoServiceTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(8.dp))
                AutoServiceTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirm(fullName, dni, phone, email)
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue
                )
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
