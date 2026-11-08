package com.torquelab.autoservice.shared.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun OperationChoice(label: String, value: String, choices: List<Pair<String, String>>, onChange: (String) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${choices.find { it.first == value }?.second ?: "Seleccione"}")
        }
        DropdownMenu(expanded, { expanded = false }) {
            choices.forEach { (key, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onChange(key); expanded = false }) }
        }
    }
}

@Composable
internal fun OperationDialog(title: String, busy: Boolean, error: String?, valid: Boolean, onClose: () -> Unit, onSave: () -> Unit, saveLabel: String = "Guardar", content: @Composable ColumnScope.() -> Unit) {
    AlertDialog(onDismissRequest = { if (!busy) onClose() }, title = { Text(title) }, text = {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            content()
        }
    }, confirmButton = { TextButton(onClick = onSave, enabled = valid && !busy) { Text(saveLabel) } },
        dismissButton = { TextButton(onClick = onClose, enabled = !busy) { Text("Cerrar") } })
}
