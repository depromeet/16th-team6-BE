package com.deepromeet.atcha.transit.infrastructure.client.kakao.response

import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitException
import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoResponse(
    val documents: List<KakaoRegionResponse>
)

data class KakaoRegionResponse(
    @JsonProperty("region_type")
    val regionType: String,
    val code: String,
    @JsonProperty("address_name")
    val addressName: String,
    @JsonProperty("region_1depth_name")
    val region1depthName: String,
    @JsonProperty("region_2depth_name")
    val region2depthName: String,
    @JsonProperty("region_3depth_name")
    val region3depthName: String,
    @JsonProperty("region_4depth_name")
    val region4depthName: String,
    val x: Double,
    val y: Double
) {
    fun toServiceRegion(): ServiceRegion {
        return when (region1depthName) {
            "서울특별시" -> ServiceRegion.SEOUL
            "경기도" -> ServiceRegion.GYEONGGI
            else -> throw TransitException.ServiceAreaNotSupported
        }
    }
}
