package com.deepromeet.atcha.transit.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.RouteItinerary

interface TransitRouteSearchClient {
    fun searchRoutes(
        start: Coordinate,
        end: Coordinate
    ): List<RouteItinerary>
}
