package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException

enum class ServiceRegion(
    val regionName: String
) {
    SEOUL("서울특별시"),
    GYEONGGI("경기도");

    companion object {
        fun from(regionName: String): ServiceRegion {
            return entries.firstOrNull { it.regionName == regionName }
                ?: throw TransitException.of(
                    TransitError.SERVICE_AREA_NOT_SUPPORTED,
                    "지역 '$regionName'은 현재 서비스를 지원하지 않습니다."
                )
        }
    }

    fun isNotSupported(): Boolean {
        return (this in listOf(SEOUL, GYEONGGI)).not()
    }
}
