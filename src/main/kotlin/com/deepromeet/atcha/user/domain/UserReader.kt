package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.user.exception.UserException
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userJpaRepository: UserJpaRepository
) {
    fun read(id: Long): User =
        userJpaRepository.findByIdAndIsDeletedFalse(id)
            ?: throw UserException.UserNotFound

    fun readByProviderId(providerId: Long): User =
        userJpaRepository.findByProviderIdAndIsDeletedFalse(providerId)
            ?: throw UserException.UserNotFound

    fun checkExists(providerId: Long): Boolean = userJpaRepository.existsByProviderId(providerId)
}
