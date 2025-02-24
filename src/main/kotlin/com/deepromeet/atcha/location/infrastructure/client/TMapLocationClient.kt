package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.domain.POIFinder
import com.deepromeet.atcha.location.domain.ReverseLabeler
import org.springframework.stereotype.Component

@Component
class TMapLocationClient(
    private val tMapLocationFeignClient: TMapLocationFeignClient
) : POIFinder, ReverseLabeler {
    override fun find(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        tMapLocationFeignClient.getPOIs(
            keyword,
            currentCoordinate.lat,
            currentCoordinate.lon
        ).let {
            return it.toPOIs()
        }
    }

    override fun label(coordinate: Coordinate): Location {
        tMapLocationFeignClient.getReverseGeoLabel(
            coordinate.lat,
            coordinate.lon
        ).let {
            return it.toLocation()
        }
    }
}
