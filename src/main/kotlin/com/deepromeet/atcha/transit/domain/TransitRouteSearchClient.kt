package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary

interface TransitRouteSearchClient {
    fun searchRoutes(
        start: Coordinate,
        end: Coordinate
    ): List<Itinerary>
}
