package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.domain.Email
import com.deepromeet.atcha.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, UserJpaRepository> {
    fun findByEmail(email: Email): User?
}