package com.deepromeet.atcha.user.domain

interface UserRepository {
    fun findById(userId: UserId): User?

    fun findByProviderId(providerId: String): User?

    fun existsByProviderId(providerId: String): Boolean

    fun save(user: User): User

    fun delete(userId: UserId)
}
