package com.deepromeet.atcha.auth.infrastructure.provider.apple

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class AppleHttpClientConfig(
    @Value("\${apple.api.url}") private val appleApiUrl: String
) {
    @Bean
    fun appleWebClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
            .baseUrl(appleApiUrl)
            .build()
    }

    @Bean
    fun appleHttpClient(appleWebClient: WebClient): AppleHttpClient {
        val adapter = WebClientAdapter.create(appleWebClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(AppleHttpClient::class.java)
    }
}
