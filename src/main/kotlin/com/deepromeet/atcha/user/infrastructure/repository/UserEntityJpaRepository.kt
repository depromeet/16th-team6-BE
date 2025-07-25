package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.infrastructure.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * UserEntity를 위한 JPA Repository
 * Infrastructure Layer의 데이터 접근 인터페이스
 */
interface UserEntityJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByIdAndIsDeletedFalse(id: Long): UserEntity?

    fun existsByProviderIdAndIsDeletedFalse(providerId: String): Boolean

    fun findByProviderIdAndIsDeletedFalse(providerId: String): UserEntity?
}
