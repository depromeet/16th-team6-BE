package com.deepromeet.atcha.user.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException
import org.springframework.boot.logging.LogLevel

enum class UserError(
    override val status: Int,
    override val errorCode: String,
    override val message: String,
    override val logLevel: LogLevel
) : BaseErrorType {
    NOTIFICATION_NOT_FOUND(404, "USR_001", "해당 ID의 알림이 없습니다.", LogLevel.WARN),
    USER_NOT_FOUND(404, "USR_002", "해당 유저 정보가 존재하지 않습니다.", LogLevel.WARN)
}

sealed class UserException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {
    data object NotificationNotFound : UserException(UserError.NOTIFICATION_NOT_FOUND) {
        override fun readResolve(): Any = NotificationNotFound
    }

    data object UserNotFound : UserException(UserError.USER_NOT_FOUND) {
        override fun readResolve(): Any = UserNotFound
    }
}
