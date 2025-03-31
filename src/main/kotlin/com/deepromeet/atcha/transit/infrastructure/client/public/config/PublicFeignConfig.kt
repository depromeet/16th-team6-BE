package com.deepromeet.atcha.transit.infrastructure.client.public.config

import org.springframework.beans.factory.annotation.Value

class PublicFeignConfig(
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) {
//    @Bean
//    fun openApiRequestInterceptor(): RequestInterceptor {
//        return RequestInterceptor { requestTemplate ->
//            requestTemplate.header("serviceKey", serviceKey)
//        }
//    }
}
