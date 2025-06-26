package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.user.exception.UserError
import com.deepromeet.atcha.user.exception.UserException
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userJpaRepository: UserJpaRepository
) {
    fun read(id: Long): User =
        userJpaRepository.findByIdAndIsDeletedFalse(id)
            ?: throw UserException.of(
                UserError.USER_NOT_FOUND,
                "ID $id 에 해당하는 사용자를 찾을 수 없습니다"
            )

    fun readByProviderId(providerId: String): User =
        userJpaRepository.findByProviderIdAndIsDeletedFalse(providerId)
            ?: throw UserException.of(
                UserError.USER_NOT_FOUND,
                "Provider ID $providerId 에 해당하는 사용자를 찾을 수 없습니다"
            )

    fun checkExists(providerId: String): Boolean = userJpaRepository.existsByProviderIdAndIsDeletedFalse(providerId)
}
