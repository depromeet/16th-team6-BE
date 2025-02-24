package com.deepromeet.atcha.user.domain

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReader: UserReader
) {
    fun getUser(email: Email): User {
        return userReader.read(email)
    }
}
