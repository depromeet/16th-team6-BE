package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.FeignDecoratorsFactory
import feign.Feign
import feign.RequestInterceptor
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

class PublicFeignConfig(
    props: OpenApiProps,
    private val registry: PublicRateLimiterRegistry,
    private val decoratorsFactory: FeignDecoratorsFactory
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
    fun publicFeignBuilder(context: ApplicationContext): Feign.Builder {
        return decoratorsFactory.builder(CircuitBreakerType.PUBLIC_API, context.displayName)
    }
}
