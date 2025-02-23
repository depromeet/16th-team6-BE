package com.deepromeet.atcha.user.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class UserErrorType(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NOTIFICATION_NOT_FOUND(404, "NTF_001", "해당 ID의 알림이 없습니다", LogLevel.WARN),
    ;
}

sealed class UserException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {

    data object NotFound : UserException(UserErrorType.NOTIFICATION_NOT_FOUND) {
        override fun readResolve(): Any = NotFound
    }
}
