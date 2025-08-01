package com.deepromeet.atcha.auth.infrastructure.repository

import com.deepromeet.atcha.auth.infrastructure.entity.UserProviderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserProviderEntityJpaRepository : JpaRepository<UserProviderEntity, Long> {
    @Query("SELECT up FROM UserProviderEntity up WHERE up.user.id = :userId")
    fun findByUserId(userId: Long): UserProviderEntity?
}
