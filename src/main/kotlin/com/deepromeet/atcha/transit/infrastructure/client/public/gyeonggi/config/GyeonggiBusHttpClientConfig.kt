package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.PublicGyeonggiBusPositionHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.PublicGyeonggiBusRealTimeInfoHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.PublicGyeonggiRouteInfoHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class GyeonggiBusHttpClientConfig(
    @Value("\${open-api.api.url.gyeonggi-route}") private val gyeonggiRouteUrl: String,
    @Value("\${open-api.api.url.gyeonggi-arrival}") private val gyeonggiArrivalUrl: String,
    @Value("\${open-api.api.url.gyeonggi-bus-position}") private val gyeonggiPositionUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun gyeonggiRouteWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(gyeonggiRouteUrl)
            .defaultHeader("Accept", "application/xml")
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "GyeonggiRoute"))
            .build()
    }

    @Bean
    fun gyeonggiArrivalWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(gyeonggiArrivalUrl)
            .defaultHeader("Accept", "application/xml")
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "GyeonggiArrival"))
            .build()
    }

    @Bean
    fun gyeonggiBusPositionWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(gyeonggiPositionUrl)
            .defaultHeader("Accept", "application/xml")
            .filter(
                circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "GyeonggiBusPosition")
            )
            .build()
    }

    @Bean
    fun publicGyeonggiRouteInfoHttpClient(gyeonggiRouteWebClient: WebClient): PublicGyeonggiRouteInfoHttpClient {
        val adapter = WebClientAdapter.create(gyeonggiRouteWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicGyeonggiRouteInfoHttpClient::class.java)
    }

    @Bean
    fun publicGyeonggiBusRealTimeInfoHttpClient(
        gyeonggiArrivalWebClient: WebClient
    ): PublicGyeonggiBusRealTimeInfoHttpClient {
        val adapter = WebClientAdapter.create(gyeonggiArrivalWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicGyeonggiBusRealTimeInfoHttpClient::class.java)
    }

    @Bean
    fun publicGyeonggiBusPositionHttpClient(
        gyeonggiBusPositionWebClient: WebClient
    ): PublicGyeonggiBusPositionHttpClient {
        val adapter = WebClientAdapter.create(gyeonggiBusPositionWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicGyeonggiBusPositionHttpClient::class.java)
    }
}
