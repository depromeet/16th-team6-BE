package com.deepromeet.atcha.shared.logging.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class DiscordError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    DISCORD_LOG_DELIVERY_FAILURE(500, "DIS_001", "Discord Log 전송에 실패했습니다.", LogLevel.ERROR)
}

class DiscordException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): DiscordException {
            return DiscordException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): DiscordException {
            return DiscordException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): DiscordException {
            return DiscordException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): DiscordException {
            return DiscordException(errorType, customMessage = message, cause = cause)
        }
    }
}
