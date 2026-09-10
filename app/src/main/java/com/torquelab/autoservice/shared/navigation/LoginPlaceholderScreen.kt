package com.torquelab.autoservice.shared.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.torquelab.autoservice.ui.theme.AutoServiceTheme

@Composable
fun LoginPlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "AutoService Login")
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPlaceholderScreenPreview() {
    AutoServiceTheme {
        LoginPlaceholderScreen()
    }
}
