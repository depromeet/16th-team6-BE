package com.deepromeet.atcha.location.infrastructure

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.CoordinateTransformer
import org.locationtech.proj4j.BasicCoordinateTransform
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.ProjCoordinate
import org.springframework.stereotype.Component

@Component
class Proj4CoordinateTransformer : CoordinateTransformer {
    override fun transformToWGS84(
        x: String,
        y: String
    ): Coordinate {
        val crsFactory = CRSFactory()
        val sourceCRS = crsFactory.createFromName("EPSG:5174")
        val targetCRS = crsFactory.createFromName("EPSG:4326") // WGS 84

        val srcCoord = ProjCoordinate(x.toDouble(), y.toDouble())
        val targetCoord = ProjCoordinate()

        val transform = BasicCoordinateTransform(sourceCRS, targetCRS)
        transform.transform(srcCoord, targetCoord)

        return Coordinate(targetCoord.y, targetCoord.x)
    }
}
