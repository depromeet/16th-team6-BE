package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.UserId

/**
 * UserProvider 도메인 Repository 인터페이스
 * JPA 의존성이 제거된 순수한 도메인 인터페이스
 */
interface UserProviderRepository {
    fun save(userProvider: UserProvider): UserProvider

    fun findByUserId(userId: UserId): UserProvider?

    fun findById(id: UserProviderId): UserProvider?

    fun delete(userProvider: UserProvider)

    fun existsByUserId(userId: UserId): Boolean
}
