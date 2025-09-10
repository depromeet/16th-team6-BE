package com.deepromeet.atcha.shared.infrastructure.http

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class HttpInterfaceClientConfig {
    @Bean
    fun commonWebClient(): WebClient {
        return WebClient.builder()
            .codecs { codecs ->
                codecs.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB
            }
            .filter(HttpDecodingErrorLogger.logDecodingError())
            .filter { request, next ->
                next.exchange(request)
                    .onErrorMap(WebClientResponseException::class.java) { ex ->
                        HttpErrorHandler.handleException(ex)
                    }
            }
            .build()
    }

    @Bean
    fun commonHttpServiceProxyFactory(commonWebClient: WebClient): HttpServiceProxyFactory {
        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(commonWebClient))
            .build()
    }
}
