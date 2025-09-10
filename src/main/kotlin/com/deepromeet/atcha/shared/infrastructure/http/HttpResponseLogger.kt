package com.deepromeet.atcha.shared.infrastructure.http

import com.deepromeet.atcha.shared.utils.PrettyBodyFormatter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

class HttpResponseLogger {
    companion object {
        fun logResponse(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { response ->
                if (log.isWarnEnabled()) {
                    response.bodyToMono(String::class.java)
                        .defaultIfEmpty("")
                        .doOnNext { body ->
                            try {
                                val prettyBody =
                                    PrettyBodyFormatter.format(
                                        body.toByteArray(StandardCharsets.UTF_8),
                                        response.headers().asHttpHeaders()
                                    )
                                log.debug {
                                    """
                                    |=== HTTP Response ===
                                    |Status: ${response.statusCode()}
                                    |Headers: ${response.headers().asHttpHeaders()}
                                    |Body: $prettyBody
                                    |====================
                                    """.trimMargin()
                                }
                            } catch (e: Exception) {
                                log.warn(e) { "응답 로깅 중 오류 발생" }
                            }
                        }
                        .then(Mono.just(response))
                } else {
                    Mono.just(response)
                }
            }
        }

        fun logRequest(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofRequestProcessor { request ->
                if (log.isDebugEnabled()) {
                    log.debug {
                        """
                        |=== HTTP Request ===
                        |Method: ${request.method()}
                        |URL: ${request.url()}
                        |Headers: ${request.headers()}
                        |===================
                        """.trimMargin()
                    }
                }
                Mono.just(request)
            }
        }
    }
}
