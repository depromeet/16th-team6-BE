package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.location.domain.Coordinate

/**
 * 순수 도메인 User 객체
 * 기술적 의존성(JPA, 외부 도메인)을 완전히 제거한 버전
 */
data class User(
    val id: UserId,
    val providerId: String,
    val homeAddress: HomeAddress?,
    val alertFrequencies: Set<Int>,
    val fcmToken: String?,
    val isDeleted: Boolean = false
) {
    /**
     * 사용자 활성 상태 확인
     */
    fun isActive(): Boolean = !isDeleted

    /**
     * 집 주소가 설정되어 있는지 확인
     */
    fun hasHomeAddress(): Boolean = homeAddress != null

    /**
     * 알림 설정 여부 확인
     */
    fun hasAlertFrequencies(): Boolean = alertFrequencies.isNotEmpty()

    /**
     * FCM 토큰 설정 여부 확인
     */
    fun hasFcmToken(): Boolean = !fcmToken.isNullOrBlank()

    /**
     * 집 주소를 좌표로 변환
     */
    fun getHomeCoordinate(): Coordinate {
        require(homeAddress != null) { "Home address is not set" }
        return Coordinate(homeAddress.latitude, homeAddress.longitude)
    }

    /**
     * 집 주소 업데이트
     */
    fun updateHomeAddress(newAddress: HomeAddress): User {
        return copy(homeAddress = newAddress)
    }

    /**
     * 알림 빈도 업데이트
     */
    fun updateAlertFrequencies(frequencies: Set<Int>): User {
        require(frequencies.all { it > 0 }) { "Alert frequencies must be positive" }
        return copy(alertFrequencies = frequencies)
    }

    /**
     * FCM 토큰 업데이트
     */
    fun updateFcmToken(newToken: String?): User {
        return copy(fcmToken = newToken)
    }

    /**
     * 사용자 삭제 (소프트 삭제)
     */
    fun markAsDeleted(): User {
        return copy(isDeleted = true)
    }

    companion object {
        /**
         * 새로운 사용자 생성 팩토리 메서드
         */
        fun create(
            id: UserId,
            providerId: String,
            homeAddress: HomeAddress? = null,
            alertFrequencies: Set<Int> = emptySet(),
            fcmToken: String? = null
        ): User {
            require(providerId.isNotBlank()) { "Provider ID cannot be blank" }

            return User(
                id = id,
                providerId = providerId,
                homeAddress = homeAddress,
                alertFrequencies = alertFrequencies,
                fcmToken = fcmToken,
                isDeleted = false
            )
        }
    }
}
