package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.user.exception.UserException
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userJpaRepository: UserJpaRepository
) {
    fun checkExists(providerId: Long): Boolean = userJpaRepository.existsByProviderId(providerId)

    fun save(user: User): User = userJpaRepository.save(user)

    fun findById(id: Long): User =
        userJpaRepository.findById(id)
            .orElseThrow { UserException.UserNotFound }

    fun findByProviderId(providerId: Long): User =
        userJpaRepository.findByProviderId(providerId)
            ?: throw UserException.UserNotFound
}
