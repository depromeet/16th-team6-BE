package com.deepromeet.atcha.shared.domain.event.domain

interface EventHandler {
    fun supports(eventType: String): Boolean

    fun handle(eventPayload: String)
}
