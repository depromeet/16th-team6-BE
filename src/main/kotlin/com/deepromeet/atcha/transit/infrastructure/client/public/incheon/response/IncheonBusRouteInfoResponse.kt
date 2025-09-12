package com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.TransitTimeParser
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusServiceHours
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import com.deepromeet.atcha.transit.domain.bus.BusTravelTimeCalculator
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class IncheonBusRouteInfoResponse(
    @JacksonXmlProperty(localName = "ADMINNM")
    val adminName: String,
    @JacksonXmlProperty(localName = "DEST_BSTOPID")
    val destinationStationId: String,
    @JacksonXmlProperty(localName = "DEST_BSTOPNM")
    val destinationStationName: String,
    @JacksonXmlProperty(localName = "FBUS_DEPHMS")
    val firstBusTime: String,
    @JacksonXmlProperty(localName = "LBUS_DEPHMS")
    val lastBusTime: String,
    @JacksonXmlProperty(localName = "MAX_ALLOCGAP")
    val maxDispatchInterval: Int,
    @JacksonXmlProperty(localName = "MIN_ALLOCGAP")
    val minDispatchInterval: Int,
    @JacksonXmlProperty(localName = "ORIGIN_BSTOPID")
    val originStationId: String,
    @JacksonXmlProperty(localName = "ORIGIN_BSTOPNM")
    val originStationName: String,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String,
    @JacksonXmlProperty(localName = "ROUTELEN")
    val routeLength: String,
    @JacksonXmlProperty(localName = "ROUTENO")
    val routeNumber: String,
    @JacksonXmlProperty(localName = "ROUTETPCD")
    val routeType: String,
    @JacksonXmlProperty(localName = "TURN_BSTOPID")
    val turnStationId: String,
    @JacksonXmlProperty(localName = "TURN_BSTOPNM")
    val turnStationName: String
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(routeId),
            name = routeNumber,
            serviceRegion = ServiceRegion.INCHEON
        )
    }

    fun toBusSchedule(busRouteInfo: BusRouteInfo): BusSchedule {
        val busRouteStation = busRouteInfo.targetStation
        val travelTime = BusTravelTimeCalculator.calculate(busRouteStation, false)

        return BusSchedule(
            busRouteInfo = busRouteInfo,
            busStation = busRouteStation.busStation,
            busTimeTable =
                BusTimeTable(
                    firstTime =
                        TransitTimeParser.parseTime(
                            firstBusTime.padStart(4, '0'),
                            LocalDate.now(),
                            TIME_FORMATTER
                        ).plusMinutes(travelTime),
                    lastTime =
                        TransitTimeParser.parseTime(
                            lastBusTime.padStart(4, '0'),
                            LocalDate.now(),
                            TIME_FORMATTER
                        ).plusMinutes(travelTime),
                    term = maxDispatchInterval
                )
        )
    }

    fun toBusRouteOperationInfo(): BusRouteOperationInfo {
        return BusRouteOperationInfo(
            originStationName,
            destinationStationName,
            serviceHours =
                listOf(
                    BusServiceHours(
                        dailyType = DailyType.WEEKDAY,
                        startTime =
                            TransitTimeParser.parseTime(
                                firstBusTime,
                                LocalDate.now(),
                                TIME_FORMATTER
                            ),
                        endTime =
                            TransitTimeParser.parseTime(
                                lastBusTime,
                                LocalDate.now(),
                                TIME_FORMATTER
                            ),
                        term = maxDispatchInterval
                    )
                )
        )
    }

    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm")
    }
}
