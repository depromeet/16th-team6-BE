package com.deepromeet.atcha.transit.domain

enum class ServiceRegion(
    val regionName: String
) {
    SEOUL("서울특별시"),
    GYEONGGI("경기도");

    companion object {
        fun from(regionName: String): ServiceRegion {
            return entries.firstOrNull { it.regionName == regionName }
                ?: throw IllegalArgumentException("Invalid region name: $regionName")
        }
    }
}
