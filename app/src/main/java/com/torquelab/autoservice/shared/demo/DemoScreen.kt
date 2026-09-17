package com.torquelab.autoservice.shared.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.torquelab.autoservice.BuildConfig
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.management.ManagementRoute

@Composable
fun DemoScreen(onExit: () -> Unit) {
    if (!BuildConfig.DEBUG) return
    var fleet by rememberSaveable { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Surface(color = MaterialTheme.colorScheme.tertiaryContainer) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text(stringResource(R.string.m_demo_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.m_demo_notice), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onExit) { Text(stringResource(R.string.m_demo_exit)) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = fleet, onClick = { fleet = true }, label = { Text(stringResource(R.string.nav_vehicles)) })
            FilterChip(selected = !fleet, onClick = { fleet = false }, label = { Text(stringResource(R.string.nav_inventory)) })
        }
        Box(Modifier.weight(1f)) { ManagementRoute(isFleet = fleet, demo = true) }
    }
}
