package com.deepromeet.atcha.route.infrastructure.client.tmap

import com.deepromeet.atcha.route.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.TMapRouteResponse
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.fallback.CircuitBreakerOpenException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class TMapRouteClientFallbackFactory : FallbackFactory<TMapRouteClient> {
    private val logger = KotlinLogging.logger {}

    override fun create(cause: Throwable): TMapRouteClient {
        return object : TMapRouteClient {
            override fun getRoutes(request: TMapRouteRequest): TMapRouteResponse {
                logger.error(cause) { "TMap route service is unavailable, circuit breaker opened" }
                throw CircuitBreakerOpenException("TMap route service is currently unavailable", cause)
            }
        }
    }
}
