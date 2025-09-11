package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class HttpRateLimitFilter(
    props: OpenApiProps,
    private val registry: PublicRateLimiterRegistry
) {
    private val urlKeyMap: Map<String, String> =
        props.api.url.entries.associate { (k, v) -> v to k }

    fun rateLimitFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
            Mono.fromCallable {
                val baseUrl = request.url().toString()
                registry.awaitByUrl(baseUrl, urlKeyMap)
            }.subscribeOn(Schedulers.boundedElastic())
                .then(next.exchange(request))
        }
    }
}
