package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayStationId
import com.deepromeet.atcha.transit.domain.SubwayStationMeta
import com.deepromeet.atcha.transit.domain.SubwayTime
import com.deepromeet.atcha.transit.infrastructure.client.public.config.ItemDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class PublicJsonResponse<T>(
    val response: Response<T>
)

data class Response<T>(
    val header: Header,
    val body: Body<T>
)

data class Header(
    val resultCode: String,
    val resultMsg: String
)

data class Body<T>(
    @JsonDeserialize(using = ItemDeserializer::class)
    val items: Items<T>?,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)

data class Items<T>(
    val item: T
)

data class SubwayStationResponse(
    val subwayRouteName: String,
    val subwayStationId: String,
    val subwayStationName: String
) {
    fun toData(): SubwayStationData =
        SubwayStationData(
            id = SubwayStationId(subwayStationId),
            info = SubwayStationMeta(subwayStationName, subwayRouteName)
        )
}

data class SubwayTimeResponse(
    val dailyTypeCode: String,
    val depTime: String,
    val endSubwayStationId: String?,
    val endSubwayStationNm: String?,
    val subwayStationId: String,
    val subwayStationNm: String,
    val upDownTypeCode: String
) {
    fun toDomain(endStation: SubwayStation): SubwayTime? {
        return SubwayTime(
            finalStation = endStation,
            departureTime = parseTime(depTime) ?: return null,
            subwayDirection = SubwayDirection.fromCode(upDownTypeCode)
        )
    }

    private fun parseTime(time: String): LocalDateTime? {
        if (time == "0") {
            return null
        }

        val formatter = DateTimeFormatter.ofPattern("HHmmss")
        val localTime = LocalTime.parse(time, formatter)
        val now = LocalDate.now()

        // 자정 이후 새벽 시간대 (00:00:00 ~ 03:59:59)는 "다음 날"로 설정
        val date = if (localTime.hour < 4) now.plusDays(1) else now

        return LocalDateTime.of(date, localTime)
    }
}
