package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.UserId

interface UserProviderRepository {
    fun save(userProvider: UserProvider): UserProvider

    fun findByUserId(userId: UserId): UserProvider?

    fun findById(id: UserProviderId): UserProvider?

    fun delete(userProvider: UserProvider)

    fun existsByUserId(userId: UserId): Boolean
}
