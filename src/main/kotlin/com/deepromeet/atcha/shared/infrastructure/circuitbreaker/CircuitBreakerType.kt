package com.deepromeet.atcha.shared.infrastructure.circuitbreaker

enum class CircuitBreakerType(val instanceName: String) {
    PUBLIC_API("public-api"),
    COMMERCIAL_API("commercial-api"),
    AUTH_API("auth-api")
}
