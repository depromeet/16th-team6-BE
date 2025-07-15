package com.deepromeet.atcha.transit.infrastructure.client.tmap

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.region.RegionIdentifier
import com.deepromeet.atcha.transit.domain.region.ServiceRegion
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
