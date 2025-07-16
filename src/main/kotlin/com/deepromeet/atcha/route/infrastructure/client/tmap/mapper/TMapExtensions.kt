package com.deepromeet.atcha.route.infrastructure.client.tmap.mapper

import com.deepromeet.atcha.route.domain.RouteItinerary
import com.deepromeet.atcha.route.domain.RouteLeg
import com.deepromeet.atcha.route.domain.RouteLocation
import com.deepromeet.atcha.route.domain.RouteMode
import com.deepromeet.atcha.route.domain.RoutePassStop
import com.deepromeet.atcha.route.domain.RoutePassStops
import com.deepromeet.atcha.route.domain.RouteStep
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.Itinerary
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.Leg
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.PassStopList
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.Station
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.Step
import com.deepromeet.atcha.transit.domain.TransitInfo

fun Itinerary.toDomain(): RouteItinerary {
    return RouteItinerary(
        totalTime = this.totalTime,
        transferCount = this.transferCount,
        totalWalkDistance = this.totalWalkDistance,
        totalDistance = this.totalDistance,
        totalWalkTime = this.totalWalkTime,
        totalFare = this.fare.regular.totalFare,
        legs = this.legs.map { it.toDomain() },
        pathType = this.pathType
    )
}

fun Leg.toDomain(): RouteLeg {
    return RouteLeg(
        distance = this.distance,
        sectionTime = this.sectionTime,
        mode = RouteMode.from(this.mode),
        route = this.route,
        routeColor = this.routeColor,
        type = this.type.toString(),
        service = this.service.toString(),
        start = this.start.toDomain(),
        end = this.end.toDomain(),
        steps = this.steps?.map { it.toDomain() },
        passStops = this.passStopList?.toDomain(),
        pathCoordinates = this.passShape?.linestring,
        transitInfo = TransitInfo.NoInfoTable
    )
}

fun Location.toDomain(): RouteLocation {
    return RouteLocation.of(this.name, this.lat, this.lon)
}

fun Step.toDomain(): RouteStep {
    return RouteStep(
        distance = this.distance,
        streetName = this.streetName,
        description = this.description,
        linestring = this.linestring
    )
}

fun Station.toRoutePassStop(): RoutePassStop {
    return RoutePassStop.of(
        index = this.index,
        latitude = this.lat.toDouble(),
        longitude = this.lon.toDouble(),
        stationName = this.stationName
    )
}

fun PassStopList.toDomain(): RoutePassStops {
    return RoutePassStops.of(
        this.stationList.map { it.toRoutePassStop() }
    )
}
