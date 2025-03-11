package com.deepromeet.atcha.transit.infrastructure.client.tmap.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class TMapFeignConfig(
    @Value("\${tmap.api.app-key}")
    private val appKey: String
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            requestTemplate.header("appKey", appKey)
                .header("accept", "application/json")
                .header("content-type", "application/json")
        }
    }
}
