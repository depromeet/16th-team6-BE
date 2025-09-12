package com.deepromeet.atcha.shared.infrastructure.http

import com.deepromeet.atcha.shared.exception.ExternalApiError
import com.deepromeet.atcha.shared.exception.ExternalApiException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.reactive.function.client.WebClientResponseException

private val log = KotlinLogging.logger {}

class HttpErrorHandler {
    companion object {
        fun handleException(ex: WebClientResponseException): ExternalApiException {
            val requestUrl = ex.request?.uri?.toString() ?: "알 수 없는 URL"
            val requestMethod = ex.request?.method?.name() ?: "알 수 없는 메서드"

            val detailedMessage =
                """
                - 외부 API 호출 오류 -
                - 요청: $requestMethod $requestUrl
                - 응답 상태: ${ex.statusCode} ${ex.statusText}
                - 응답 본문: ${ex.responseBodyAsString}
                """.trimIndent()

            return when (ex.statusCode.value()) {
                400 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_BAD_REQUEST_ERROR, detailedMessage)
                403 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_FORBIDDEN_ERROR, detailedMessage)
                404 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_NOT_FOUND_ERROR, detailedMessage)
                429 -> ExternalApiException.of(ExternalApiError.EXTERNAL_API_CALL_LIMIT_EXCEEDED, detailedMessage)
                500, 502, 503 ->
                    ExternalApiException.of(
                        ExternalApiError.EXTERNAL_API_INTERNAL_SERVER_ERROR,
                        detailedMessage
                    )
                else -> {
                    log.warn { "정의되지 않은 외부 API 오류 발생. $detailedMessage" }
                    ExternalApiException.of(ExternalApiError.EXTERNAL_API_UNKNOWN_ERROR, detailedMessage)
                }
            }
        }
    }
}
