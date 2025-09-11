package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.CircuitBreakerType
import com.deepromeet.atcha.shared.infrastructure.circuitbreaker.WebClientCircuitBreakerFactory
import com.deepromeet.atcha.transit.infrastructure.client.public.common.PublicHolidayHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class PublicHolidayHttpClientConfig(
    @Value("\${open-api.api.url.holiday}") private val holidayApiUrl: String,
    private val circuitBreakerFactory: WebClientCircuitBreakerFactory
) {
    @Bean
    fun publicHolidayWebClient(publicApiWebClient: WebClient): WebClient {
        return publicApiWebClient.mutate()
            .baseUrl(holidayApiUrl)
            .filter(circuitBreakerFactory.createCircuitBreakerFilter(CircuitBreakerType.PUBLIC_API, "PublicHoliday"))
            .build()
    }

    @Bean
    fun publicHolidayHttpClient(publicHolidayWebClient: WebClient): PublicHolidayHttpClient {
        val adapter = WebClientAdapter.create(publicHolidayWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PublicHolidayHttpClient::class.java)
    }
}
