package com.deepromeet.atcha.transit.infrastructure.client.odsay.config

import com.deepromeet.atcha.transit.infrastructure.client.odsay.ODSayBusHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class ODSayBusHttpClientConfig {
    @Bean
    fun odsayBusHttpClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${odsay.api.url}") baseUrl: String
    ): ODSayBusHttpClient {
        val webClient =
            webClientBuilder
                .baseUrl(baseUrl)
                .build()

        val factory =
            HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build()

        return factory.createClient(ODSayBusHttpClient::class.java)
    }
}
