package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.exception.LocationError
import com.deepromeet.atcha.location.exception.LocationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LocationReader(
    private val poiFinder: POIFinder,
    private val reverseGeocoder: ReverseGeocoder
) {
    fun readPOIs(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        try {
            if (keyword.isBlank()) {
                return emptyList()
            }
            return poiFinder.find(keyword, currentCoordinate)
        } catch (e: FeignException) {
            logger.error(e) { "POI 정보 읽기 실패: ${e.message}" }
            throw LocationException.of(
                LocationError.FAILED_TO_READ_POIS,
                "키워드 '$keyword'로 POI 검색 중 오류가 발생했습니다.",
                e
            )
        }
    }

    fun read(coordinate: Coordinate): Location {
        try {
            return reverseGeocoder.geocode(coordinate)
        } catch (e: FeignException) {
            logger.error(e) { "위치 정보 읽기 실패: ${e.message}" }
            throw LocationException.of(
                LocationError.FAILED_TO_READ_LOCATION,
                "좌표 (${coordinate.lat}, ${coordinate.lon})의 위치 정보를 읽어오는 중 오류가 발생했습니다.",
                e
            )
        }
    }
}
