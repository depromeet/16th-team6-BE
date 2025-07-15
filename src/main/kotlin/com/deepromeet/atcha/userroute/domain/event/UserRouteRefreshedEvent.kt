package com.deepromeet.atcha.userroute.domain.event

import com.deepromeet.atcha.shared.domain.event.domain.DomainEvent
import com.deepromeet.atcha.userroute.domain.UserRoute
import java.time.LocalDateTime
import java.util.UUID

data class UserRouteRefreshedEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val eventType: String = "USER_ROUTE_REFRESHED",
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
    override val aggregateId: String,
    val userRoute: UserRoute
) : DomainEvent
