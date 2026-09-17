package com.torquelab.autoservice.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.torquelab.autoservice.shared.ui.theme.AutoServiceSpacing

@Composable
fun AutoServiceEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                AutoServiceSpacing.Large
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                AutoServiceSpacing.Small,
                Alignment.CenterVertically
            )
    ) {

        Text(
            text = title,
            style =
                MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}