package com.deepromeet.atcha.common.web

import com.deepromeet.atcha.common.exception.CustomException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.logging.LogLevel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        exception: CustomException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        when (exception.errorType.logLevel) {
            LogLevel.ERROR -> logger.error(exception) { exception.message }
            LogLevel.WARN -> logger.warn(exception) { exception.message }
            else -> logger.info(exception) { exception.message }
        }
        return ResponseEntity.status(exception.status)
            .body(
                ApiResponse.error(
                    exception.errorType.errorCode,
                    request.requestURI,
                    exception.message,
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.error(exception) { "알 수 없는 서버 에러입니다" }
        return ResponseEntity.internalServerError()
            .body(
                ApiResponse.error(
                    "INTERNAL_SERVER_ERROR",
                    exception.message
                        ?: "알 수 없는 서버 에러입니다",
                )
            )
    }
}
