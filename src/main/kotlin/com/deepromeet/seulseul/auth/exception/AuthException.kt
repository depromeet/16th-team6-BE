package com.deepromeet.seulseul.auth.exception

import com.deepromeet.seulseul.common.exception.DomainException

sealed class AuthException(
    errorCode: AuthErrorType
) : DomainException(errorCode) {

    data object NoMatchedProvider : AuthException(AuthErrorType.NO_MATCHED_PROVIDER) {
        private fun readResolve(): Any = NoMatchedProvider
    }

    data object AlreadyExistsUser : AuthException(AuthErrorType.ALREADY_EXISTS_USER) {
        private fun readResolve(): Any = NoMatchedProvider
    }
}
