package com.deepromeet.atcha.user.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import com.deepromeet.atcha.common.exception.ErrorReason
import org.springframework.boot.logging.LogLevel

enum class UserErrorType(
    private val status: Int,
    private val errorCode: String,
    private val message: String,
    override val logLevel: LogLevel,
) : BaseErrorType {
    NOTIFICATION_NOT_FOUND(404, "NTF_001", "해당 ID의 알림이 없습니다", LogLevel.WARN),
    ;

    override val errorReason: ErrorReason
        get() = ErrorReason(status, errorCode, message)
}

sealed class UserException(
    errorCode: BaseErrorType,
) : CustomException(errorCode) {
    data object NotFound : UserException(UserErrorType.NOTIFICATION_NOT_FOUND) {
        override fun readResolve(): Any = NotFound
    }
}
