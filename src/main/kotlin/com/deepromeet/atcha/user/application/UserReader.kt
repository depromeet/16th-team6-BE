package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserRepository
import com.deepromeet.atcha.user.exception.UserError
import com.deepromeet.atcha.user.exception.UserException
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepository: UserRepository
) {
    fun read(id: Long): User =
        userRepository.findById(UserId(id))
            ?: throw UserException.of(
                UserError.USER_NOT_FOUND,
                "ID $id 에 해당하는 사용자를 찾을 수 없습니다"
            )

    fun readByProviderId(providerId: String): User =
        userRepository.findByProviderId(providerId)
            ?: throw UserException.of(
                UserError.USER_NOT_FOUND,
                "Provider ID $providerId 에 해당하는 사용자를 찾을 수 없습니다"
            )

    fun checkExists(providerId: String): Boolean = userRepository.existsByProviderId(providerId)
}
