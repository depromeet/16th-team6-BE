package com.deepromeet.atcha.user.domain

/**
 * 집 주소 값 객체 (순수 도메인 객체)
 * JPA 의존성 제거된 버전
 */
data class HomeAddress(
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(address.isNotBlank()) { "Address cannot be blank" }
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }

    /**
     * 좌표 정보를 Location 도메인과 독립적으로 처리
     * 인터페이스를 통해 결합도 낮춤
     */
    fun getCoordinates(): Pair<Double, Double> = latitude to longitude

    companion object {
        fun empty() = HomeAddress("", 0.0, 0.0)
    }
}
