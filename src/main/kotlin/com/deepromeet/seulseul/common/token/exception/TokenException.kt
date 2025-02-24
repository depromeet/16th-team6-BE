package com.deepromeet.seulseul.common.token.exception

import com.deepromeet.seulseul.common.exception.WebException

sealed class TokenException (
    errorType: TokenErrorType
) : WebException(errorType) {

    data object ExpiredToken : TokenException(TokenErrorType.EXPIRED_TOKEN) {
        private fun readResolve(): Any = ExpiredToken
    }

    data object NotValidToken : TokenException(TokenErrorType.NOT_VALID_TOKEN) {
        private fun readResolve(): Any = NotValidToken
    }
}
