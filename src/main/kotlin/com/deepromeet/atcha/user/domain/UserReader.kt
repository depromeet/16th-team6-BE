package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.user.exception.UserException
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userJpaRepository: UserJpaRepository
) {
    fun checkExists(kakaoId: Long): Boolean = userJpaRepository.existsByKakaoId(kakaoId)

    fun save(user: User): User = userJpaRepository.save(user)

    fun findById(id: Long): User =
        userJpaRepository.findById(id)
            .orElseThrow { UserException.UserNotFound }

    fun findByKakaoId(kakaoId: Long): User =
        userJpaRepository.findByKakaoId(kakaoId)
            ?: throw UserException.UserNotFound
}
