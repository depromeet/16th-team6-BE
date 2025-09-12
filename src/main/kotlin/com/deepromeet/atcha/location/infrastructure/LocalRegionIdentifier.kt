package com.deepromeet.atcha.location.infrastructure

import com.deepromeet.atcha.location.application.RegionIdentifier
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.location.infrastructure.repository.RegionBoundaryRepository
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class LocalRegionIdentifier(
    private val repo: RegionBoundaryRepository
) : RegionIdentifier {
    override suspend fun identify(coordinate: Coordinate): ServiceRegion {
        val p =
            repo.sridFactory.createPoint(
                org.locationtech.jts.geom.Coordinate(coordinate.lon, coordinate.lat)
            )
        val hit =
            repo.all().firstOrNull { it.geom.covers(p) }
                ?: throw TransitException.of(
                    TransitError.SERVICE_AREA_NOT_SUPPORTED,
                    "서비스 지역이 아닙니다: (${coordinate.lon}, ${coordinate.lat})"
                )

        return ServiceRegion.from(hit.name)
    }
}
