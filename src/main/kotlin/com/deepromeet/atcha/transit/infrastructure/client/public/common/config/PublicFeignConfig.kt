package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean

class PublicFeignConfig(
    props: OpenApiProps,
    private val registry: RateLimiterRegistry
) {
    private val urlKeyMap: Map<String, String> =
        props.api.url.entries.associate { (k, v) -> v to k }

    @Bean
    fun rateLimitInterceptor(): RequestInterceptor =
        RequestInterceptor { template ->
            val baseUrl = template.feignTarget().url() + template.url()
            registry.awaitByUrl(baseUrl, urlKeyMap)
        }
}
