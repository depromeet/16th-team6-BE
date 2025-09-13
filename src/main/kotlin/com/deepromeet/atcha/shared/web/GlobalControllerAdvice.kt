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

private val log = KotlinLogging.logger {}

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
            RequestException.of(
                RequestError.NO_MATCHED_METHOD,
                "지원하지 않는 HTTP 메서드입니다: ${httpException.method}." +
                    " 지원되는 메서드: ${httpException.supportedMethods?.joinToString(", ") +
                        " 요청 URL: ${request.requestURI}. "}",
                httpException
            )
        return handle(exception, request)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        noResourceException: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val exception =
            RequestException.of(
                RequestError.NO_MATCHED_RESOURCE,
                "요청하신 리소스를 찾을 수 없습니다: ${noResourceException.resourcePath}"
            )
        return handle(exception, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val rootCause = findRootCause(exception)

        return when (rootCause) {
            is IllegalArgumentException -> {
                val customException =
                    RequestException.of(
                        RequestError.INVALID_REQUEST,
                        rootCause.message ?: "잘못된 요청 파라미터입니다",
                        rootCause
                    )
                handle(customException, request)
            }
            is IllegalStateException -> {
                val customException =
                    RequestException.of(
                        RequestError.INVALID_REQUEST,
                        rootCause.message ?: "현재 상태에서 처리할 수 없는 요청입니다",
                        rootCause
                    )
                handle(customException, request)
            }
            else -> {
                log.error(exception) { "알 수 없는 서버 에러입니다" }
                ResponseEntity.internalServerError()
                    .body(
                        ApiResponse.error(
                            "INTERNAL_SERVER_ERROR",
                            exception.cause?.message ?: "알 수 없는 서버 에러입니다"
                        )
                    )
            }
        }
    }

    private fun handle(
        exception: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        when (exception.errorType.logLevel) {
            LogLevel.ERROR -> log.error(exception) { exception.message }
            LogLevel.WARN -> log.warn(exception) { exception.message }
            else -> log.info(exception) { exception.message }
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

    private fun findRootCause(throwable: Throwable): Throwable {
        var cause = throwable
        while (cause.cause != null && cause.cause != cause) {
            cause = cause.cause!!
        }
        return cause
    }
}
