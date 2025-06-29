package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.common.feign.FeignException
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.domain.POIFinder
import com.deepromeet.atcha.location.domain.ReverseGeocoder
import com.deepromeet.atcha.location.exception.LocationError
import com.deepromeet.atcha.location.exception.LocationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class TMapLocationClient(
    private val tMapLocationFeignClient: TMapLocationFeignClient
) : POIFinder, ReverseGeocoder {
    override fun find(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        try {
            return tMapLocationFeignClient.getPOIs(
                keyword,
                currentCoordinate.lat,
                currentCoordinate.lon
            )?.toPOIs() ?: emptyList()
        } catch (e: FeignException) {
            logger.error(e) { "TMap POI 검색 실패 (키워드: $keyword): ${e.message}" }
            throw LocationException.of(
                LocationError.FAILED_TO_READ_POIS,
                e.message ?: "TMap POI 검색 중 오류, 발생",
                e
            )
        }
    }

    override fun geocode(coordinate: Coordinate): Location {
        try {
            val poiInfo =
                tMapLocationFeignClient.getReverseLabel(
                    coordinate.lat,
                    coordinate.lon
                )

            val address =
                tMapLocationFeignClient.getReverseGeocoding(
                    coordinate.lat,
                    coordinate.lon
                )

            return Location(
                name = poiInfo.poiInfo.name,
                address = address.addressInfo.fullAddress,
                coordinate = coordinate
            )
        } catch (e: FeignException) {
            logger.error(e) { "TMap 좌표 변환 실패 (좌표: $coordinate): ${e.message}" }
            throw LocationException.of(
                LocationError.FAILED_TO_READ_LOCATION,
                e.message ?: "TMap 좌표 변환 중 오류 발생",
                e
            )
        }
    }
}
