package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.domain.POIFinder
import org.springframework.stereotype.Component

@Component
class TMapLocationClient(
    private val tMapLocationFeignClient: TMapLocationFeignClient
) : POIFinder {
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
}
