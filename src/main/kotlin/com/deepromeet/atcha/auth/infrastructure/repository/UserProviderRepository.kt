package com.deepromeet.atcha.auth.infrastructure.repository

import com.deepromeet.atcha.auth.domain.UserProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserProviderRepository : JpaRepository<UserProvider, Long> {
    @Query("SELECT up FROM UserProvider up WHERE up.user.id = :userId")
    fun findByUserId(userId: Long): UserProvider?
}
