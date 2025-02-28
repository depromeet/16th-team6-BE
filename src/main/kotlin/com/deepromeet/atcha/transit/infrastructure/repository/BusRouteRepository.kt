package com.deepromeet.atcha.transit.infrastructure.repository

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.RouteId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class BusRouteRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun findByRouteName(routeName: String): BusRoute? {
        val sql =
            """
            SELECT route_id, route_name
            FROM bus_routes
            WHERE route_name = ?
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, routeName).firstOrNull()
    }

    private val rowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            BusRoute(
                routeId = RouteId(rs.getInt("route_id")),
                routeName = rs.getString("route_name")
            )
        }
}
