package com.deepromeet.atcha.common.web

import com.deepromeet.atcha.common.exception.CustomException
import com.deepromeet.atcha.common.web.exception.RequestException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.logging.LogLevel
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalControllerAdvice {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        exception: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> = handle(exception, request)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(request: HttpServletRequest): ResponseEntity<ApiResponse<Unit>> {
        val exception = RequestException.NoMatchedMethod
        return handle(exception, request)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(request: HttpServletRequest): ResponseEntity<ApiResponse<Unit>> {
        val exception = RequestException.NoMatchedResource
        return handle(exception, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error(exception) { "알 수 없는 서버 에러입니다" }
        return ResponseEntity.internalServerError()
            .body(
                ApiResponse.error(
                    "INTERNAL_SERVER_ERROR",
                    exception.cause?.message
                        ?: "알 수 없는 서버 에러입니다"
                )
            )
    }

    private fun handle(
        exception: CustomException,
        request: HttpServletRequest
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
                    exception.message
                )
            )
    }
}
