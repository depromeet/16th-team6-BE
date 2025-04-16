package com.deepromeet.atcha.common.logging.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

//@Component
//@Profile("prod")
class ProdLoggingInterceptor : BaseLoggingInterceptor() {
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (request.requestURI.contains(IGNORE_URI)) {
            return
        }

        val duration = System.currentTimeMillis() - MDC.get(REQUEST_TIME).toLong()
        val status =
            when (response.status / 100) {
                2, 3 -> "SUCCESS"
                else -> "FAIL"
            }

        logger.info { "[${MDC.get(REQUEST_ID)}] [$status] [${request.method} ${request.requestURI}] [$duration ms]" }
    }
}
