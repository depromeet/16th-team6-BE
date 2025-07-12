package com.deepromeet.atcha.app.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class AppError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NO_MATCHED_PLATFORM(404, "APP_001", "일치하는 플랫폼이 없습니다.", LogLevel.WARN)
}

sealed class AppException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object NoMatchedPlatForm : AppException(AppError.NO_MATCHED_PLATFORM) {
        override fun readResolve(): Any = NoMatchedPlatForm
    }
}
