package com.deepromeet.seulseul.user.exception

import com.deepromeet.seulseul.common.exception.BaseErrorType
import com.deepromeet.seulseul.common.exception.DomainException

sealed class UserException(
    errorCode: BaseErrorType
) : DomainException(errorCode) {

    data object NotFound : UserException(UserErrorType.NOTIFICATION_NOT_FOUND) {
        private fun readResolve(): Any = NotFound
    }

}