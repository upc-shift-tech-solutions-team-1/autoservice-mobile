package com.torquelab.autoservice.shared.domain

fun isWebEvidence(value: String): Boolean = runCatching {
    val uri = java.net.URI(value.trim())
    uri.scheme?.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank() && uri.userInfo == null
}.getOrDefault(false)
