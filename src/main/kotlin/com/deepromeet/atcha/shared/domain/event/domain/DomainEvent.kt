package com.deepromeet.atcha.shared.domain.event.domain

import java.time.LocalDateTime

interface DomainEvent {
    val eventId: String
    val eventType: String
    val occurredAt: LocalDateTime
    val aggregateId: String
}
