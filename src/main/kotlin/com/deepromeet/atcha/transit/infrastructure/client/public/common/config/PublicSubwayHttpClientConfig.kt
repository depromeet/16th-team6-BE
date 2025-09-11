package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import com.deepromeet.atcha.transit.infrastructure.client.public.common.PublicSubwayInfoHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.common.PublicSubwayScheduleHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class PublicSubwayHttpClientConfig(
    @Value("\${open-api.api.url.subway}") private val publicApiUrl: String,
    @Value("\${open-api.api.url.subway-schedule}") private val subwayScheduleUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun publicSubwayWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(publicApiUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "PublicSubway"))
            .build()
    }

    @Bean
    fun publicSubwayScheduleWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(subwayScheduleUrl)
            .filter(
                circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "PublicSubwaySchedule")
            )
            .build()
    }

    @Bean
    fun publicSubwayInfoHttpClient(publicSubwayWebClient: WebClient): PublicSubwayInfoHttpClient {
        val adapter = WebClientAdapter.create(publicSubwayWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicSubwayInfoHttpClient::class.java)
    }

    @Bean
    fun publicSubwayScheduleHttpClient(publicSubwayScheduleWebClient: WebClient): PublicSubwayScheduleHttpClient {
        val adapter = WebClientAdapter.create(publicSubwayScheduleWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicSubwayScheduleHttpClient::class.java)
    }
}
