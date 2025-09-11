package com.deepromeet.atcha.location.infrastructure.client.config

import com.deepromeet.atcha.location.infrastructure.client.TMapReverseGeoHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class TMapReverseGeoHttpClientConfig {
    @Bean
    fun tMapReverseGeoHttpClient(tmapWebClient: WebClient): TMapReverseGeoHttpClient {
        val factory =
            HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(tmapWebClient))
                .build()

        return factory.createClient(TMapReverseGeoHttpClient::class.java)
    }
}
