package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, Long> {
    fun findByIdAndIsDeletedFalse(id: Long): User?

    fun existsByProviderIdAndIsDeletedFalse(providerId: String): Boolean

    fun findByProviderIdAndIsDeletedFalse(providerId: String): User?
}
