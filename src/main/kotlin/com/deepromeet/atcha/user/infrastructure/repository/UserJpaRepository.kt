package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, Long> {
    fun existsByKakaoId(id: Long): Boolean

    fun findByKakaoId(kakaoId: Long): User?
}
