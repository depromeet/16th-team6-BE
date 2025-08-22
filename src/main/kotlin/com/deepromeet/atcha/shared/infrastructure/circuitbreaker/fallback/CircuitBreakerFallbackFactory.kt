package com.deepromeet.atcha.shared.infrastructure.circuitbreaker.fallback

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class CircuitBreakerFallbackFactory<T> : FallbackFactory<T> {
    private val logger = KotlinLogging.logger {}

    override fun create(cause: Throwable): T {
        logger.error(cause) { "Circuit breaker fallback triggered" }
        throw CircuitBreakerOpenException("External service is currently unavailable", cause)
    }
}

class CircuitBreakerOpenException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
