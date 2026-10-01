package com.torquelab.autoservice.customer_trust.domain.model

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrackingProgressTest {

    private lateinit var activeOrderTasks: List<TrackingTask>
    private lateinit var pendingTask: TrackingTask

    @Before
    fun setUp() {
        activeOrderTasks = listOf(
            createTask(1, TrackingTaskStatus.COMPLETED),
            createTask(2, TrackingTaskStatus.IN_PROGRESS),
            createTask(3, TrackingTaskStatus.COMPLETED)
        )
        pendingTask = createTask(4, TrackingTaskStatus.PENDING)
    }

    @Test
    fun activeOrderProgressReflectsCompletedTasksAndAvoidsFalseProgressWithoutTasks() {
        val progress = calculateTrackingProgress(
            TrackingOrderStatus.IN_PROGRESS,
            activeOrderTasks
        )
        val progressWithoutTasks = calculateTrackingProgress(
            TrackingOrderStatus.IN_PROGRESS,
            emptyList()
        )

        assertEquals(67, progress)
        assertEquals(0, progressWithoutTasks)
    }

    @Test
    fun finishedOrDeliveredOrderReportsCompleteProgress() {
        val finishedProgress = calculateTrackingProgress(
            TrackingOrderStatus.FINISHED,
            listOf(pendingTask)
        )
        val deliveredProgress = calculateTrackingProgress(
            TrackingOrderStatus.DELIVERED,
            listOf(pendingTask)
        )

        assertEquals(100, finishedProgress)
        assertEquals(100, deliveredProgress)
    }

    private fun createTask(id: Int, status: TrackingTaskStatus) = TrackingTask(
        id = id,
        description = "Service task",
        status = status,
        technicalDiagnosis = null,
        customerExplanation = null,
        evidenceRegistered = null,
        laborPrice = null,
        parts = emptyList()
    )
}
