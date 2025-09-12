package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.application.POIFinder
import com.deepromeet.atcha.location.application.ReverseGeocoder
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI
import org.springframework.stereotype.Component

@Component
class TMapLocationClient(
    private val tmapLocationHttpClient: TMapLocationHttpClient
) : POIFinder, ReverseGeocoder {
    override suspend fun find(
        keyword: String,
        currentCoordinate: Coordinate
    ): List<POI> {
        return tmapLocationHttpClient.getPOIs(
            keyword,
            currentCoordinate.lat,
            currentCoordinate.lon
        )?.toPOIs() ?: emptyList()
    }

    override suspend fun geocode(coordinate: Coordinate): Location {
        val poiInfo =
            tmapLocationHttpClient.getReverseLabel(
                coordinate.lat,
                coordinate.lon
            )

        val address =
            tmapLocationHttpClient.getReverseGeocoding(
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
