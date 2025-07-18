package com.deepromeet.atcha.shared.exception

import org.springframework.boot.logging.LogLevel

enum class InfrastructureError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    CACHE_ERROR(500, "INF_001", "캐시 서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),
    DATABASE_ERROR(500, "INF_002", "데이터베이스에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR)
}

class InfrastructureException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): InfrastructureException {
            return InfrastructureException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): InfrastructureException {
            return InfrastructureException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): InfrastructureException {
            return InfrastructureException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): InfrastructureException {
            return InfrastructureException(errorType, customMessage = message, cause = cause)
        }
    }
}
