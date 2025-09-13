package com.deepromeet.atcha.location.infrastructure.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.prep.PreparedGeometry
import org.locationtech.jts.geom.prep.PreparedGeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class RegionBoundaryRepository(
    @Value("classpath:data/seoul_incheon_gyeonggi_polygon.geojson")
    private val geojson: Resource
) {
    private val log = KotlinLogging.logger {}

    data class RegionGeom(
        val code: String,
        val name: String,
        val geom: PreparedGeometry
    )

    private val objectMapper = jacksonObjectMapper()
    private val geoReader = GeoJsonReader()

    // 지연 로딩
    private val regions: List<RegionGeom> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        log.debug { "Loading region geoms" }
        loadRegions()
    }

    fun all(): List<RegionGeom> = regions

    private fun loadRegions(): List<RegionGeom> {
        val text = geojson.inputStream.bufferedReader().use { it.readText() }
        val root = objectMapper.readTree(text) // FeatureCollection
        val features = root["features"]

        return features.map { f ->
            val props = f["properties"]
            val code = props["SIDO_CD"].asText() // "11" / "28" / "41"
            val name = props["SIDO_NM"].asText() // "서울특별시" / "인천광역시" / "경기도"
            val geomJson = f["geometry"].toString() // Polygon/MultiPolygon
            val raw: Geometry = geoReader.read(geomJson)

            RegionGeom(code, name, PreparedGeometryFactory.prepare(raw))
        }
    }

    val sridFactory = GeometryFactory(PrecisionModel(), 4326)
}
