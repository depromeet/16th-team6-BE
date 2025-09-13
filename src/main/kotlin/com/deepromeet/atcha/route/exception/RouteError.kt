package com.deepromeet.atcha.route.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class RouteError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    USER_ROUTE_NOT_FOUND(404, "URT_001", "해당 유저가 등록한 경로를 찾을 수 없습니다.", LogLevel.WARN),
    USER_ROUTE_REFRESH_ERROR(500, "URT_002", "현재 출발 시간을 갱신 할 수 없습니다.", LogLevel.ERROR),
    INVALID_LAST_ROUTE(500, "LRT_001", "유효하지 않은 막차 경로입니다.", LogLevel.ERROR),
    INVALID_ROUTE_MODE(500, "LRT_002", "지원하지 않는 모드입니다.", LogLevel.ERROR),
    LAST_ROUTES_NOT_FOUND(404, "LRT_003", "유효한 막차 경로를 찾을 수 없습니다.", LogLevel.WARN),
    NO_AVAILABLE_LAST_ROUTES(404, "LRT_004", "시간이 늦어서 탑승 가능한 막차 경로가 없습니다.", LogLevel.WARN),
    INVALID_LAST_TIME(400, "LRT_005", "04:00 ~ 20:00 사이의 시간은 유효하지 않습니다.", LogLevel.WARN)
}

class RouteException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): RouteException {
            return RouteException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): RouteException {
            return RouteException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): RouteException {
            return RouteException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): RouteException {
            return RouteException(errorType, customMessage = message, cause = cause)
        }
    }
}
