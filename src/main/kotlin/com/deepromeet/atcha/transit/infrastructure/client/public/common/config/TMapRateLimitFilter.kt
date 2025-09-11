package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class TMapRateLimitFilter(
    private val registry: PublicRateLimiterRegistry
) {
    fun rateLimitFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
            Mono.fromCallable {
                registry.awaitForTMap()
            }.subscribeOn(Schedulers.boundedElastic())
                .then(next.exchange(request))
        }
    }
}
