package com.deepromeet.atcha.transit.infrastructure.client.public.incheon.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.PublicIncheonBusArrivalHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.PublicIncheonBusPositionHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.PublicIncheonBusRouteInfoHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class IncheonBusHttpClientConfig(
    @Value("\${open-api.api.url.incheon-route}") private val incheonRouteUrl: String,
    @Value("\${open-api.api.url.incheon-arrival}") private val incheonArrivalUrl: String,
    @Value("\${open-api.api.url.incheon-position}") private val incheonPositionUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun incheonRouteWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(incheonRouteUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "IncheonRoute"))
            .build()
    }

    @Bean
    fun incheonArrivalWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(incheonArrivalUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "IncheonArrival"))
            .build()
    }

    @Bean
    fun incheonPositionWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(incheonPositionUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "IncheonPosition"))
            .build()
    }

    @Bean
    fun publicIncheonBusRouteInfoHttpClient(incheonRouteWebClient: WebClient): PublicIncheonBusRouteInfoHttpClient {
        val adapter = WebClientAdapter.create(incheonRouteWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicIncheonBusRouteInfoHttpClient::class.java)
    }

    @Bean
    fun publicIncheonBusArrivalHttpClient(incheonArrivalWebClient: WebClient): PublicIncheonBusArrivalHttpClient {
        val adapter = WebClientAdapter.create(incheonArrivalWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicIncheonBusArrivalHttpClient::class.java)
    }

    @Bean
    fun publicIncheonBusPositionHttpClient(incheonPositionWebClient: WebClient): PublicIncheonBusPositionHttpClient {
        val adapter = WebClientAdapter.create(incheonPositionWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicIncheonBusPositionHttpClient::class.java)
    }
}
