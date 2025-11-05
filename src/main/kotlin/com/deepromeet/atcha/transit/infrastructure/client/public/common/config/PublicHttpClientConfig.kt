package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.shared.infrastructure.http.HttpResponseLogger
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(OpenApiProps::class)
class PublicHttpClientConfig(
    private val rateLimitFilter: HttpRateLimitFilter
) {
    @Bean
    fun publicApiWebClient(customWebClientBuilder: WebClient.Builder): WebClient {
        return customWebClientBuilder
            .filter(rateLimitFilter.rateLimitFilter())
            .filter(HttpResponseLogger.logRequest())
//            .filter(HttpResponseLogger.logResponse())
            .build()
    }
}
