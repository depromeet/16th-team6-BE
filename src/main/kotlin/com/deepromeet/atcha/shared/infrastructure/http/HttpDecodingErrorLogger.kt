package com.deepromeet.atcha.shared.infrastructure.http

import com.deepromeet.atcha.shared.utils.PrettyBodyFormatter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

class HttpDecodingErrorLogger {
    companion object {
        fun logDecodingError(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { response ->
                // 응답 상태가 성공이지만 디코딩 과정에서 실패할 수 있는 경우를 대비
                if (response.statusCode().is2xxSuccessful) {
                    // 응답 본문을 미리 읽어서 캐시
                    response.bodyToMono(String::class.java)
                        .defaultIfEmpty("")
                        .map { bodyContent ->
                            // 디코딩 에러 발생 시 로깅할 수 있도록 응답에 속성으로 저장
                            response.mutate()
                                .headers { headers ->
                                    headers.add("X-Original-Body", bodyContent)
                                }
                                .build()
                        }
                        .onErrorResume { error ->
                            // 디코딩 실패 시 응답 내용 로깅
                            response.bodyToMono(String::class.java)
                                .defaultIfEmpty("")
                                .doOnNext { bodyContent ->
                                    val prettyBody =
                                        PrettyBodyFormatter.format(
                                            bodyContent.toByteArray(StandardCharsets.UTF_8),
                                            response.headers().asHttpHeaders()
                                        )
                                    log.warn(error) {
                                        "응답 디코딩 실패\n=== Response Body ===\n$prettyBody\n===================="
                                    }
                                }
                                .then(Mono.error<ClientResponse>(error))
                        }
                } else {
                    Mono.just(response)
                }
            }
        }
    }
}
