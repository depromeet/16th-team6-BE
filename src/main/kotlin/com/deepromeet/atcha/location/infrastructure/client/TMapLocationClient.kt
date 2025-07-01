package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.domain.POIFinder
import com.deepromeet.atcha.location.domain.ReverseGeocoder
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
        return tMapLocationFeignClient.getPOIs(
            keyword,
            currentCoordinate.lat,
            currentCoordinate.lon
        )?.toPOIs() ?: emptyList()
    }

    override fun geocode(coordinate: Coordinate): Location {
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
    }
}
