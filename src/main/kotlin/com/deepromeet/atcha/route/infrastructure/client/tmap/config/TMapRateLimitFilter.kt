package com.deepromeet.atcha.route.infrastructure.client.tmap.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import java.time.Duration

@Component
class TMapRateLimitFilter {
    private val bucket =
        Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(20)
                    .refillGreedy(20, Duration.ofSeconds(1))
                    .build()
            )
            .build()

    fun rateLimitFilter(): ExchangeFilterFunction {
        return ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
            mono {
                while (true) {
                    val probe = bucket.tryConsumeAndReturnRemaining(1)
                    if (probe.isConsumed) break
                    val waitMillis = (probe.nanosToWaitForRefill / 1_000_000).coerceAtLeast(1)
                    delay(waitMillis)
                }
            }.then(next.exchange(request))
        }
    }
}
