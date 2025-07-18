package com.deepromeet.atcha.shared.web

import com.deepromeet.atcha.shared.exception.CustomException
import com.deepromeet.atcha.shared.web.exception.RequestError
import com.deepromeet.atcha.shared.web.exception.RequestException
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
    fun handleHttpRequestMethodNotSupportedException(
        httpException: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val exception =
            RequestException.Companion.of(
                RequestError.NO_MATCHED_METHOD,
                "지원하지 않는 HTTP 메서드입니다: ${httpException.method}. 지원되는 메서드: ${httpException.supportedMethods?.joinToString(
                    ", "
                )}"
            )
        return handle(exception, request)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        noResourceException: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val exception =
            RequestException.Companion.of(
                RequestError.NO_MATCHED_RESOURCE,
                "요청하신 리소스를 찾을 수 없습니다: ${noResourceException.resourcePath}"
            )
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
                    exception.message ?: exception.errorType.message
                )
            )
    }
}
