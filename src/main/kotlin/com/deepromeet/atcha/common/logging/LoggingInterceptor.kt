package com.deepromeet.atcha.common.logging

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception
import java.util.UUID

@Component
class LoggingInterceptor : HandlerInterceptor {
    companion object {
        private const val REQUEST_ID = "requestId"
        private const val REQUEST_TIME = "requestTime"
        private val logger = KotlinLogging.logger {}
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        MDC.put(REQUEST_ID, UUID.randomUUID().toString().substring(0, 8))
        MDC.put(REQUEST_TIME, System.currentTimeMillis().toString())

        logger.info { "REQUEST [${MDC.get(REQUEST_ID)}] [${request.method} ${request.requestURI}]" }
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val duration = System.currentTimeMillis() - MDC.get(REQUEST_TIME).toLong()
        logger.info { "RESPONSE [${MDC.get(REQUEST_ID)}] [${request.method} ${request.requestURI}] [$duration ms]" }
    }
}
