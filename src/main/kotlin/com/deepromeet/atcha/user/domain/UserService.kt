package com.deepromeet.atcha.user.domain

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReader: UserReader
) {
}
