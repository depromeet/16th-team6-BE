package com.deepromeet.atcha.transit.infrastructure.client.openapi.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class OpenAPIFeignConfig(
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) {
    @Bean
    fun openApiRequestInterceptor(): RequestInterceptor {
        println("serviceKey: $serviceKey")
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("serviceKey", serviceKey)
        }
    }
}
