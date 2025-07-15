package com.deepromeet.atcha.route.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class UserRouteError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    USER_ROUTE_NOT_FOUND(404, "URT_001", "해당 유저가 등록한 경로를 찾을 수 없습니다.", LogLevel.ERROR),
    USER_ROUTE_REFRESH_ERROR(500, "URT_002", "현재 출발 시간을 갱신 할 수 없습니다.", LogLevel.ERROR)
}

class UserRouteException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): UserRouteException {
            return UserRouteException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): UserRouteException {
            return UserRouteException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): UserRouteException {
            return UserRouteException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): UserRouteException {
            return UserRouteException(errorType, customMessage = message, cause = cause)
        }
    }
}
