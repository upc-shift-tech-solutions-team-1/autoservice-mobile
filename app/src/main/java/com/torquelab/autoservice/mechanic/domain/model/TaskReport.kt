package com.torquelab.autoservice.mechanic.domain.model

import com.torquelab.autoservice.shared.domain.isWebEvidence

data class TaskReport(
    val technicalDiagnosis: String,
    val customerExplanation: String,
    val internalObservation: String,
    val evidenceRegistered: String
) {
    fun validationError(): String? = when {
        evidenceRegistered.isNotBlank() && !isWebEvidence(evidenceRegistered) -> "La evidencia debe ser una dirección HTTP o HTTPS válida."
        else -> null
    }
}
