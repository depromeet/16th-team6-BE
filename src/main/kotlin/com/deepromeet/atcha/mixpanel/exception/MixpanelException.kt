package com.deepromeet.atcha.mixpanel.exception

import com.deepromeet.atcha.shared.exception.BaseErrorType
import com.deepromeet.atcha.shared.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class MixpanelError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    MIXPANEL_EVENT_DELIVERY_FAILURE(500, "MIX_001", "Mixpanel 이벤트 전송에 실패했습니다.", LogLevel.ERROR)
}

class MixpanelException(
    errorCode: BaseErrorType,
    customMessage: String? = null,
    cause: Throwable? = null
) : CustomException(errorCode, customMessage, cause) {
    override fun readResolve(): Any = this

    companion object {
        fun of(errorType: BaseErrorType): MixpanelException {
            return MixpanelException(errorType)
        }

        fun of(
            errorType: BaseErrorType,
            message: String
        ): MixpanelException {
            return MixpanelException(errorType, customMessage = message)
        }

        fun of(
            errorType: BaseErrorType,
            cause: Throwable
        ): MixpanelException {
            return MixpanelException(errorType, cause = cause)
        }

        fun of(
            errorType: BaseErrorType,
            message: String,
            cause: Throwable
        ): MixpanelException {
            return MixpanelException(errorType, customMessage = message, cause = cause)
        }
    }
}
