package com.deepromeet.atcha.route.domain.event

import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.shared.domain.event.domain.DomainEvent
import java.time.LocalDateTime
import java.util.UUID

data class UserRouteRefreshedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val eventType: String = "USER_ROUTE_REFRESHED",
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
    override val aggregateId: String,
    val userRoute: UserRoute
) : DomainEvent
