package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import feign.Feign
import feign.RequestInterceptor
import io.github.resilience4j.feign.FeignDecorators
import io.github.resilience4j.feign.Resilience4jFeign
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class PublicFeignConfig(
    props: OpenApiProps,
    private val registry: PublicRateLimiterRegistry,
    private val circuitBreakerDecorators: Map<CircuitBreakerType, FeignDecorators>
) {
    private val urlKeyMap: Map<String, String> =
        props.api.url.entries.associate { (k, v) -> v to k }

    @Bean
    fun rateLimitInterceptor(): RequestInterceptor =
        RequestInterceptor { template ->
            val baseUrl = template.feignTarget().url() + template.url()
            registry.awaitByUrl(baseUrl, urlKeyMap)
        }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    fun circuitBreakerDecorator(): Feign.Builder {
        val decorator = circuitBreakerDecorators[CircuitBreakerType.PUBLIC_API]!!
        return Feign.builder()
            .addCapability(Resilience4jFeign.capability(decorator))
    }
}
