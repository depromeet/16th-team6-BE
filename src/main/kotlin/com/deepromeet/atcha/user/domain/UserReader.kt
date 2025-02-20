package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.user.exception.UserException
import com.deepromeet.atcha.user.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userJpaRepository: UserJpaRepository
) {
    fun read(id: Long): User {
        return userJpaRepository.findById(id).orElseThrow { UserException.NotFound }
    }

    fun read(email: Email): User {
        return userJpaRepository.findByEmail(email) ?: throw UserException.NotFound
    }

    fun checkExists(kakaoId: Long) : Boolean {
        return userJpaRepository.existsByKakaoId(kakaoId);
    }
 }
