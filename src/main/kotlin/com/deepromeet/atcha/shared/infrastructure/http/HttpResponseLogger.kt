package com.deepromeet.atcha.shared.infrastructure.http

import com.deepromeet.atcha.shared.utils.PrettyBodyFormatter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

class HttpResponseLogger {
    companion object {
        fun logResponse(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { response ->
                if (!log.isDebugEnabled()) {
                    return@ofResponseProcessor Mono.just(response)
                }

                val factory = DefaultDataBufferFactory.sharedInstance

                DataBufferUtils.join(response.bodyToFlux(DataBuffer::class.java))
                    .defaultIfEmpty(factory.wrap(ByteArray(0)))
                    .flatMap { joined ->
                        val bytes = ByteArray(joined.readableByteCount())
                        joined.read(bytes)
                        // 메모리 누수 방지를 위해 원본 버퍼는 해제
                        DataBufferUtils.release(joined)

                        try {
                            val prettyBody = PrettyBodyFormatter.format(bytes, response.headers().asHttpHeaders())
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

                        val rebuiltBody = factory.wrap(bytes)
                        val rebuiltResponse =
                            ClientResponse.from(response)
                                .body(if (bytes.isEmpty()) Flux.empty() else Flux.just(rebuiltBody))
                                .build()

                        Mono.just(rebuiltResponse)
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
