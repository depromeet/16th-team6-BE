package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response

import com.deepromeet.atcha.transit.domain.BusDirection
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusRouteStation
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusServiceHours
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.BusTravelTimeCalculator
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.TransitTimeParser
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GyeonggiBusRouteInfoResponse(
    @field:JacksonXmlProperty(localName = "busRouteInfoItem")
    val busRouteInfoItem: BusRouteInfoItem
)

data class BusRouteInfoItem(
    @field:JacksonXmlProperty(localName = "routeId")
    val routeId: String,
    @field:JacksonXmlProperty(localName = "routeName")
    val routeName: String,
    @field:JacksonXmlProperty(localName = "startStationId")
    val startStationId: String,
    @field:JacksonXmlProperty(localName = "startStationName")
    val startStationName: String,
    @field:JacksonXmlProperty(localName = "endStationId")
    val endStationId: String,
    @field:JacksonXmlProperty(localName = "endStationName")
    val endStationName: String,
    @field:JacksonXmlProperty(localName = "turnStID")
    val turnStID: String,
    @field:JacksonXmlProperty(localName = "turnStNm")
    val turnStNm: String,
    @field:JacksonXmlProperty(localName = "upFirstTime")
    val upFirstTime: String?,
    @field:JacksonXmlProperty(localName = "downFirstTime")
    val downFirstTime: String?,
    @field:JacksonXmlProperty(localName = "satUpFirstTime")
    val satUpFirstTime: String?,
    @field:JacksonXmlProperty(localName = "satDownFirstTime")
    val satDownFirstTime: String?,
    @field:JacksonXmlProperty(localName = "sunUpFirstTime")
    val sunUpFirstTime: String?,
    @field:JacksonXmlProperty(localName = "sunDownFirstTime")
    val sunDownFirstTime: String?,
    @field:JacksonXmlProperty(localName = "weUpFirstTime")
    val weUpFirstTime: String?,
    @field:JacksonXmlProperty(localName = "weDownFirstTime")
    val weDownFirstTime: String?,
    @field:JacksonXmlProperty(localName = "upLastTime")
    val upLastTime: String?,
    @field:JacksonXmlProperty(localName = "downLastTime")
    val downLastTime: String?,
    @field:JacksonXmlProperty(localName = "satUpLastTime")
    val satUpLastTime: String?,
    @field:JacksonXmlProperty(localName = "satDownLastTime")
    val satDownLastTime: String?,
    @field:JacksonXmlProperty(localName = "sunUpLastTime")
    val sunUpLastTime: String?,
    @field:JacksonXmlProperty(localName = "sunDownLastTime")
    val sunDownLastTime: String?,
    @field:JacksonXmlProperty(localName = "weUpLastTime")
    val weUpLastTime: String?,
    @field:JacksonXmlProperty(localName = "weDownLastTime")
    val weDownLastTime: String?,
    @field:JacksonXmlProperty(localName = "peekAlloc")
    val peekAlloc: Int,
    @field:JacksonXmlProperty(localName = "nPeekAlloc")
    val nPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "wePeekAlloc")
    val wePeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "weNPeekAlloc")
    val weNPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "satPeekAlloc")
    val satPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "satNPeekAlloc")
    val satNPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "sunPeekAlloc")
    val sunPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "sunNPeekAlloc")
    val sunNPeekAlloc: Int
) {
    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    private val termMap =
        mapOf(
            DailyType.WEEKDAY to peekAlloc,
            DailyType.SATURDAY to satPeekAlloc,
            DailyType.SUNDAY to sunPeekAlloc,
            DailyType.HOLIDAY to wePeekAlloc
        )

    fun toBusSchedule(
        dailyType: DailyType,
        routeStation: BusRouteStation
    ): BusSchedule {
        val travelTimeFromStart =
            BusTravelTimeCalculator.calculate(
                routeStation,
                hasDownTimetable(dailyType)
            )

        val busTimeTable =
            getBusTimeTable(
                dailyType,
                routeStation.getDirection(),
                travelTimeFromStart
            )

        return BusSchedule(
            busRoute =
                BusRoute(
                    id = BusRouteId(routeId),
                    name = routeName,
                    serviceRegion = ServiceRegion.GYEONGGI
                ),
            busStation = routeStation.busStation,
            busTimeTable = busTimeTable
        )
    }

    fun toBusRouteOperationInfo(): BusRouteOperationInfo {
        return BusRouteOperationInfo(
            startStationName = startStationName,
            endStationName = endStationName,
            serviceHours =
                listOfNotNull(
                    createBusServiceHours(DailyType.WEEKDAY, BusDirection.UP),
                    createBusServiceHours(DailyType.WEEKDAY, BusDirection.DOWN),
                    createBusServiceHours(DailyType.SATURDAY, BusDirection.UP),
                    createBusServiceHours(DailyType.SATURDAY, BusDirection.DOWN),
                    createBusServiceHours(DailyType.HOLIDAY, BusDirection.UP),
                    createBusServiceHours(DailyType.HOLIDAY, BusDirection.DOWN)
                )
        )
    }

    private fun getBusTimeTable(
        dailyType: DailyType,
        busDirection: BusDirection,
        travelTimeFromStart: Long
    ): BusTimeTable =
        getBusTimeStr(dailyType, busDirection).let { (first, last) ->
            BusTimeTable(
                firstTime =
                    TransitTimeParser
                        .parseTime(first, LocalDate.now(), TIME_FORMATTER)
                        .plusMinutes(travelTimeFromStart),
                lastTime =
                    TransitTimeParser
                        .parseTime(last, LocalDate.now(), TIME_FORMATTER)
                        .plusMinutes(travelTimeFromStart),
                term = termMap[dailyType] ?: 0
            )
        }

    private fun getBusTimeStr(
        dailyType: DailyType,
        busDirection: BusDirection
    ): Pair<String?, String?> {
        return when (dailyType) {
            DailyType.WEEKDAY ->
                if (busDirection == BusDirection.UP) {
                    upFirstTime to upLastTime
                } else {
                    downFirstTime to downLastTime
                }

            DailyType.SATURDAY ->
                if (busDirection == BusDirection.UP) {
                    satUpFirstTime to satUpLastTime
                } else {
                    satDownFirstTime to satDownLastTime
                }

            DailyType.SUNDAY ->
                if (busDirection == BusDirection.UP) {
                    sunUpFirstTime to sunUpLastTime
                } else {
                    sunDownFirstTime to sunDownLastTime
                }

            DailyType.HOLIDAY ->
                if (busDirection == BusDirection.UP) {
                    weUpFirstTime to weUpLastTime
                } else {
                    weDownFirstTime to weDownLastTime
                }
        }
    }

    private fun createBusServiceHours(
        dailyType: DailyType,
        busDirection: BusDirection
    ): BusServiceHours? {
        val (startTime, endTime) = getBusTimeStr(dailyType, busDirection)
        return BusServiceHours(
            dailyType = dailyType,
            busDirection = busDirection,
            startTime = parseOrNull(startTime),
            endTime = parseOrNull(endTime),
            term = termMap[dailyType] ?: 0
        )
    }

    private fun parseOrNull(time: String?) =
        runCatching { TransitTimeParser.parseTime(time, LocalDate.now(), TIME_FORMATTER) }
            .getOrNull()

    private fun hasDownTimetable(dailyType: DailyType): Boolean {
        return when (dailyType) {
            DailyType.WEEKDAY -> downFirstTime != null && downLastTime != null
            DailyType.SATURDAY -> satDownFirstTime != null && satDownLastTime != null
            DailyType.SUNDAY -> sunDownFirstTime != null && sunDownLastTime != null
            DailyType.HOLIDAY -> weDownFirstTime != null && weDownLastTime != null
        }
    }
}
