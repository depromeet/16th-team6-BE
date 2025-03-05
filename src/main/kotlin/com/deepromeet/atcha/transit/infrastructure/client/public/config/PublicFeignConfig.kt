package com.deepromeet.atcha.transit.infrastructure.client.public.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class PublicFeignConfig(
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) {
    @Bean
    fun openApiRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("serviceKey", serviceKey)
        }
    }
}
