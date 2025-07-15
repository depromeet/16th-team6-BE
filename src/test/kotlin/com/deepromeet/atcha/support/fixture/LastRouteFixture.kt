package com.deepromeet.atcha.support.fixture

import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.route.LastRoute
import com.deepromeet.atcha.transit.domain.route.LastRouteLeg
import com.deepromeet.atcha.transit.domain.route.Station
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step
import java.time.LocalDateTime

object LastRouteFixture {
    fun create(
        routeId: String = "route-123",
        departureDateTime: String = LocalDateTime.now().plusMinutes(60).toString(),
        totalTime: Int = 60,
        totalWalkTime: Int = 10,
        totalWorkDistance: Int = 500,
        transferCount: Int = 2,
        totalDistance: Int = 10000,
        pathType: Int = 1,
        legs: List<LastRouteLeg> = listOf(createLastRouteLegFixture())
    ): LastRoute {
        return LastRoute(
            routeId = routeId,
            departureDateTime = departureDateTime,
            totalTime = totalTime,
            totalWalkTime = totalWalkTime,
            totalWorkDistance = totalWorkDistance,
            transferCount = transferCount,
            totalDistance = totalDistance,
            pathType = pathType,
            legs = legs
        )
    }

    fun createLastRouteLegFixture(
        distance: Int = 3000,
        sectionTime: Int = 20,
        mode: String = "BUS",
        departureDateTime: String? = LocalDateTime.now().plusMinutes(10).toString(),
        route: String? = "BUS:1234",
        type: String? = "express",
        service: String? = "local",
        start: Location = Location(name = "StartStation", lat = 37.5665, lon = 126.9780),
        end: Location = Location(name = "EndStation", lat = 37.5796, lon = 126.9770),
        passStopList: List<Station>? =
            listOf(
                Station(
                    index = 0,
                    stationName = "MidStation",
                    lat = "37.5700",
                    lon = "126.9800"
                )
            ),
        step: List<Step>? =
            listOf(
                Step(
                    distance = 100.0,
                    streetName = "Main Road",
                    description = "Walk straight",
                    linestring = "encodedLinestringData"
                )
            ),
        passShape: String? = "encodedPolylineData",
        transitInfo: TransitInfo = TransitInfo.NoInfoTable
    ): LastRouteLeg {
        return LastRouteLeg(
            distance = distance,
            sectionTime = sectionTime,
            mode = mode,
            departureDateTime = departureDateTime,
            route = route,
            type = type,
            service = service,
            start = start,
            end = end,
            passStopList = PassStopList(passStopList!!),
            step = step,
            passShape = passShape,
            transitInfo = transitInfo
        )
    }
}
