package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.PublicSeoulBusArrivalInfoHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.PublicSeoulBusPositionHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.PublicSeoulBusRouteInfoHttpClient
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.SeoulBusOperationHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class SeoulBusHttpClientConfig(
    @Value("\${open-api.api.url.bus}") private val seoulBusUrl: String,
    @Value("\${open-api.api.url.bus-route}") private val seoulBusRouteUrl: String,
    @Value("\${open-api.api.url.bus-position}") private val seoulBusPositionUrl: String,
    @Value("\${open-api.api.url.bus-operation}") private val seoulBusOperationUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun seoulBusArrivalWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(seoulBusUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "SeoulBusArrival"))
            .build()
    }

    @Bean
    fun seoulBusRouteWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(seoulBusRouteUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "SeoulBusRoute"))
            .build()
    }

    @Bean
    fun seoulBusPositionWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(seoulBusPositionUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "SeoulBusPosition"))
            .build()
    }

    @Bean
    fun seoulBusOperationWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(seoulBusOperationUrl)
            .filter(
                circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "SeoulBusOperation")
            )
            .build()
    }

    @Bean
    fun publicSeoulBusArrivalInfoHttpClient(seoulBusArrivalWebClient: WebClient): PublicSeoulBusArrivalInfoHttpClient {
        val adapter = WebClientAdapter.create(seoulBusArrivalWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicSeoulBusArrivalInfoHttpClient::class.java)
    }

    @Bean
    fun publicSeoulBusRouteInfoHttpClient(seoulBusRouteWebClient: WebClient): PublicSeoulBusRouteInfoHttpClient {
        val adapter = WebClientAdapter.create(seoulBusRouteWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicSeoulBusRouteInfoHttpClient::class.java)
    }

    @Bean
    fun publicSeoulBusPositionHttpClient(seoulBusPositionWebClient: WebClient): PublicSeoulBusPositionHttpClient {
        val adapter = WebClientAdapter.create(seoulBusPositionWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicSeoulBusPositionHttpClient::class.java)
    }

    @Bean
    fun seoulBusOperationHttpClient(seoulBusOperationWebClient: WebClient): SeoulBusOperationHttpClient {
        val adapter = WebClientAdapter.create(seoulBusOperationWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(SeoulBusOperationHttpClient::class.java)
    }
}
