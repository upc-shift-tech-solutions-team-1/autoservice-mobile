package com.torquelab.autoservice.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.torquelab.autoservice.R
import com.torquelab.autoservice.shared.ui.theme.AutoServiceSpacing

@Composable
fun AutoServiceLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                AutoServiceSpacing.Medium,
                Alignment.CenterVertically
            )
    ) {

        CircularProgressIndicator()

        Text(
            text = stringResource(
                R.string.common_loading
            )
        )
    }
}