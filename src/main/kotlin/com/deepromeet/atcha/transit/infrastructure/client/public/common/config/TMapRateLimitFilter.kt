package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction

@Component
class TMapRateLimitFilter(
    private val registry: PublicRateLimiterRegistry
) {
    fun rateLimitFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
            mono {
                registry.awaitForTMap()
            }.then(next.exchange(request))
        }
    }
}
