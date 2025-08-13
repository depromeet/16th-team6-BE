package com.deepromeet.atcha.location.infrastructure.client

import com.deepromeet.atcha.location.application.RegionIdentifier
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion
import org.springframework.stereotype.Component

@Component
class TMapRegionClient(
    private val tMapReverseGeoFeignClient: TMapReverseGeoFeignClient
) : RegionIdentifier {
    override fun identify(coordinate: Coordinate): ServiceRegion =
        tMapReverseGeoFeignClient.getReverseGeo(
            coordinate.lat.toString(),
            coordinate.lon.toString()
        ).addressInfo.toServiceRegion()
}
