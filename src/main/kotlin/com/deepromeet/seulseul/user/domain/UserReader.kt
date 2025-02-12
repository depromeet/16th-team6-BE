package com.deepromeet.seulseul.user.domain

import com.deepromeet.seulseul.user.exception.UserException
import com.deepromeet.seulseul.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userJpaRepository: UserJpaRepository
) {
    fun read(email: Email): User {
        return userJpaRepository.findByEmail(email) ?: throw UserException.NotFound
    }
}