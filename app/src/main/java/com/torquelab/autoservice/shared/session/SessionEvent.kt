package com.torquelab.autoservice.shared.session

sealed interface SessionEvent {

    data object SessionExpired : SessionEvent
}