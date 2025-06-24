package com.deepromeet.atcha.common.logging.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper

@Component
class DefaultLoggingInterceptor : BaseLoggingInterceptor() {
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (request.requestURI.contains(IGNORE_URI)) {
            return
        }
        val requestId = MDC.get(REQUEST_ID) ?: "N/A"
        val duration = System.currentTimeMillis() - MDC.get(REQUEST_TIME).toLong()
        val status =
            when (response.status / 100) {
                2, 3 -> "SUCCESS"
                else -> "FAIL"
            }

        // 요청 헤더
        val headers = request.headerNames?.toList()?.associateWith { request.getHeader(it) } ?: emptyMap()

        // 요청 파라미터
        val params = request.parameterMap?.mapValues { it.value.joinToString(", ") } ?: emptyMap()

        // 요청 바디
        val requestBody =
            if (request is ContentCachingRequestWrapper) {
                try {
                    java.lang.String(request.contentAsByteArray, request.characterEncoding)
                } catch (e: Exception) {
                    "[Failed to read body: ${e.message}]"
                }
            } else {
                "[Request not wrapped. Cannot read body.]"
            }

        logger.info {
            """
            📦 RESPONSE $status [$requestId]
            ▶ URI: ${request.method} ${request.requestURI}
            ▶ Status: [${response.status}]
            ▶ Duration: ${duration}ms
            ▶ Headers: $headers
            ▶ Params: $params
            ▶ Body: $requestBody
            """.trimIndent()
        }
    }
}
