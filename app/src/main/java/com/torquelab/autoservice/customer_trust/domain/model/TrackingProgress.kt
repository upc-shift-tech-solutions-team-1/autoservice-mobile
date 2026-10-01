package com.torquelab.autoservice.customer_trust.domain.model

import kotlin.math.roundToInt

fun calculateTrackingProgress(status: TrackingOrderStatus, tasks: List<TrackingTask>): Int = when {
    status == TrackingOrderStatus.DELIVERED || status == TrackingOrderStatus.FINISHED -> 100
    tasks.isEmpty() -> 0
    else -> (tasks.count { it.status == TrackingTaskStatus.COMPLETED } * 100.0 / tasks.size).roundToInt()
}
