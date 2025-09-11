package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.application.RegionIdentifier
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion
import org.springframework.stereotype.Component

@Component
class TMapRegionClient(
    private val tMapLocationHttpClient: TMapLocationHttpClient
) : RegionIdentifier {
    override suspend fun identify(coordinate: Coordinate): ServiceRegion =
        tMapLocationHttpClient.getReverseGeocoding(
            coordinate.lat,
            coordinate.lon
        ).addressInfo.toServiceRegion()
}
