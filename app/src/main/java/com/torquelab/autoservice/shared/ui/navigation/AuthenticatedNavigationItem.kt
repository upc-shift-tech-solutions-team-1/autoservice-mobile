package com.torquelab.autoservice.shared.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class AuthenticatedNavigationItem(
    val key: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)