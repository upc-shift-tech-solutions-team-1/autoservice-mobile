package com.torquelab.autoservice.shared.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionEventManager @Inject constructor() {

    private val _events =
        MutableSharedFlow<SessionEvent>(
            extraBufferCapacity = 1
        )

    val events: SharedFlow<SessionEvent> =
        _events.asSharedFlow()

    fun notifySessionExpired() {
        _events.tryEmit(
            SessionEvent.SessionExpired
        )
    }
}