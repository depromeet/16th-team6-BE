package com.deepromeet.atcha.location.infrastructure

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.CoordinateTransformer
import org.locationtech.proj4j.BasicCoordinateTransform
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.ProjCoordinate
import org.springframework.stereotype.Component

@Component
class Proj4CoordinateTransformer : CoordinateTransformer {
    override fun transformToWGS84(coordinate: Coordinate): Coordinate {
        val crsFactory = CRSFactory()
        val sourceCRS = crsFactory.createFromName("EPSG:5174") // Korea 2000 / Unified CS
        val targetCRS = crsFactory.createFromName("EPSG:4326") // WGS 84

        val srcCoord = ProjCoordinate(coordinate.lon, coordinate.lat)
        val targetCoord = ProjCoordinate()

        val transform = BasicCoordinateTransform(sourceCRS, targetCRS)
        transform.transform(srcCoord, targetCoord)

        return Coordinate(targetCoord.x, targetCoord.y)
    }
}
