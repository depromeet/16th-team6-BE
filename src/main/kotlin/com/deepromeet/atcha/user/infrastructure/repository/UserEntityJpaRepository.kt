package com.deepromeet.atcha.user.infrastructure.repository

import com.deepromeet.atcha.user.infrastructure.entity.UserEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * UserEntity를 위한 JPA Repository
 * Infrastructure Layer의 데이터 접근 인터페이스
 */
interface UserEntityJpaRepository : JpaRepository<UserEntity, Long> {
    /**
     * 삭제되지 않은 사용자 조회 (ID)
     */
    fun findByIdAndIsDeletedFalse(id: Long): UserEntity?

    /**
     * 삭제되지 않은 사용자 존재 여부 확인 (Provider ID)
     */
    fun existsByProviderIdAndIsDeletedFalse(providerId: String): Boolean

    /**
     * 삭제되지 않은 사용자 조회 (Provider ID)
     */
    fun findByProviderIdAndIsDeletedFalse(providerId: String): UserEntity?

    /**
     * 활성 사용자 목록 조회 (페이징)
     */
    fun findByIsDeletedFalse(pageable: Pageable): List<UserEntity>

    /**
     * 특정 알림 빈도를 가진 활성 사용자 조회
     */
    @Query(
        """
        SELECT DISTINCT u FROM UserEntity u 
        JOIN u.alertFrequencies af 
        WHERE u.isDeleted = false AND af = :frequency
    """
    )
    fun findActiveUsersByAlertFrequency(frequency: Int): List<UserEntity>
}
