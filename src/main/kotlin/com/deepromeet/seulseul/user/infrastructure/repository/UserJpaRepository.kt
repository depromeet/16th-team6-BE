package com.deepromeet.seulseul.user.infrastructure.repository

import com.deepromeet.seulseul.user.domain.Email
import com.deepromeet.seulseul.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<User, UserJpaRepository> {
    fun findByEmail(email: Email): User?
}