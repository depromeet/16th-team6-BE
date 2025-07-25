package com.deepromeet.atcha.user.domain

/**
 * User 도메인을 위한 순수 Repository 인터페이스
 * 도메인 레이어에 위치하며 기술적 구현 세부사항에 의존하지 않음
 */
interface UserRepository {
    /**
     * 사용자 ID로 조회
     */
    fun findById(userId: UserId): User?

    /**
     * Provider ID로 조회
     */
    fun findByProviderId(providerId: String): User?

    /**
     * Provider ID 존재 여부 확인
     */
    fun existsByProviderId(providerId: String): Boolean

    /**
     * 사용자 저장
     */
    fun save(user: User): User

    /**
     * 사용자 삭제 (실제로는 소프트 삭제)
     */
    fun delete(userId: UserId)

    /**
     * 활성 사용자 목록 조회 (페이징)
     */
    fun findActiveUsers(
        offset: Int,
        limit: Int
    ): List<User>

    /**
     * 특정 알림 빈도를 가진 활성 사용자 조회
     */
    fun findActiveUsersByAlertFrequency(frequency: Int): List<User>
}
