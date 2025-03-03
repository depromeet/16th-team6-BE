package com.deepromeet.atcha.auth.infrastructure.repository

import com.deepromeet.atcha.auth.domain.UserProvider
import org.springframework.data.jpa.repository.JpaRepository

interface UserProviderRepository : JpaRepository<UserProvider, Long> {
    fun findByUserId(userId: Long): UserProvider?
}
