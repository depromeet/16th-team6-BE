package com.deepromeet.atcha.transit.infrastructure.repository

import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.RouteId
import com.deepromeet.atcha.transit.domain.StationId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class BusStationRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun findByRouteIdAndStationName(
        routeId: RouteId,
        stationName: String
    ): BusStation? {
        val sql =
            """
            SELECT route_id, station_id, station_name, ord
            FROM bus_stations
            WHERE route_id = ? AND station_name = ?
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, routeId.value, stationName).firstOrNull()
    }

    private val rowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            BusStation(
                routeId = RouteId(rs.getInt("route_id")),
                stationId = StationId(rs.getInt("station_id")),
                stationName = rs.getString("station_name"),
                order = rs.getInt("ord")
            )
        }
}
