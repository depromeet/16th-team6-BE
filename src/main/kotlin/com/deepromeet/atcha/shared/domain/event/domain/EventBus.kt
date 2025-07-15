package com.deepromeet.atcha.shared.domain.event.domain

interface EventBus {
    fun publish(event: DomainEvent)

    fun publishAll(events: List<DomainEvent>)
}
