package com.deepromeet.atcha.auth.infrastructure.repository

import com.deepromeet.atcha.auth.domain.UserToken
import org.springframework.data.jpa.repository.JpaRepository

interface UserTokenRepository : JpaRepository<UserToken, Long> {
    fun findByUserId(userId: Long): UserToken?

    fun findByRefreshToken(refreshToken: String): UserToken?

    fun findByAccessToken(accessToken: String): UserToken?
}
