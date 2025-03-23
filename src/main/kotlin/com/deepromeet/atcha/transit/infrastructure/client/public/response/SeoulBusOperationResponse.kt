package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusServiceHours
import com.deepromeet.atcha.transit.domain.DailyType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SeoulBusOperationResponse(
    val rows: List<SeoulBusOperationRow>
)

data class SeoulBusOperationRow(
    val routId: String,
    val routName: String,
    val rtCd: Int,
    val routType: String,
    val tmpOrder: Int,
    val stnFirst: String,
    val stnLast: String,
    val busIntervals: Int,
    val timeFirst: String,
    val timeLast: String,
    val satTimeFirst: String,
    val satTimeLast: String,
    val holTimeFirst: String,
    val holTimeLast: String,
    val routeDistance: Int,
    val busFee: Int,
    val busRunStatusCd: String,
    val busRunStatus: String?,
    val turnPlaceStationId: String,
    val normalDayFirstBusTime: String,
    val normalDayLastBusTime: String,
    val normalLastRouteAt: String,
    val normalLastBusId: String,
    val lowerDayFirstBusTime: String,
    val lowerDayLastBusTime: String,
    val lowerLastRouteAt: String,
    val lowerLastBusId: String,
    val timegap: Int,
    val norTerms: String,
    val satTerms: String,
    val holTerms: String,
    val routNo: String,
    val companyNm: String,
    val faxNo: String?,
    val telNo: String,
    val email: String?,
    val etcDesc: String?,
    val rn: Int,
    val rnum: Int
)

private fun parseTime(hhmm: String): LocalDateTime {
    val hour = hhmm.substring(0, 2).toIntOrNull() ?: 0
    val minute = hhmm.substring(2, 4).toIntOrNull() ?: 0
    val today = LocalDate.now()
    return LocalDateTime.of(today, LocalTime.of(hour, minute))
}

fun SeoulBusOperationResponse.toBusRouteOperationInfo(): BusRouteOperationInfo? {
    val row = rows.firstOrNull() ?: return null

    val serviceHoursList = mutableListOf<BusServiceHours>()

    serviceHoursList +=
        BusServiceHours(
            dailyType = DailyType.WEEKDAY,
            startTime = parseTime(row.timeFirst),
            endTime = parseTime(row.timeLast),
            term = row.norTerms.toIntOrNull() ?: 0
        )

    serviceHoursList +=
        BusServiceHours(
            dailyType = DailyType.SATURDAY,
            startTime = parseTime(row.satTimeFirst),
            endTime = parseTime(row.satTimeLast),
            term = row.satTerms.toIntOrNull() ?: 0
        )

    serviceHoursList +=
        BusServiceHours(
            dailyType = DailyType.HOLIDAY,
            startTime = parseTime(row.holTimeFirst),
            endTime = parseTime(row.holTimeLast),
            term = row.holTerms.toIntOrNull() ?: 0
        )

    return BusRouteOperationInfo(
        startStationName = row.stnFirst,
        endStationName = row.stnLast,
        serviceHours = serviceHoursList
    )
}
