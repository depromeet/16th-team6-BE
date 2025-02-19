package com.deepromeet.atcha.user.exception

import com.deepromeet.atcha.common.exception.BaseErrorType
import com.deepromeet.atcha.common.exception.CustomException

sealed class UserException(
    errorCode: BaseErrorType
) : CustomException(errorCode) {

    data object NotFound : UserException(UserErrorType.NOTIFICATION_NOT_FOUND) {
        private fun readResolve(): Any = NotFound
    }

}