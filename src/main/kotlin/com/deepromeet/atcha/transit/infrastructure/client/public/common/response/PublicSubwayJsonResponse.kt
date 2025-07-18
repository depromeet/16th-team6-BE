package com.deepromeet.atcha.transit.infrastructure.client.public.common.response

import com.deepromeet.atcha.transit.application.subway.SubwayStationMeta
import com.deepromeet.atcha.transit.domain.TransitTimeParser
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayStationData
import com.deepromeet.atcha.transit.domain.subway.SubwayStationId
import com.deepromeet.atcha.transit.domain.subway.SubwayTime
import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.ItemDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger { }

data class PublicSubwayJsonResponse<T>(
    val response: Response<T>
) {
    companion object {
        fun <T> isSubwayApiLimitExceeded(response: PublicSubwayJsonResponse<T>): Boolean {
            val limitMessages =
                listOf(
                    "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                    "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
                )

            val isLimited =
                response.response.header.resultCode != "00" ||
                    limitMessages.any { response.response.header.resultMsg.contains(it) }

            if (isLimited) {
                log.warn { "지하철 API 요청 수 초과: ${response.response.header.resultMsg}" }
            }

            return isLimited
        }
    }
}

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
            isExpress = false,
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

data class TrainScheduleResponse(
    val trainno: String?,
    val trainKnd: String?,
    val upbdnbSe: String,
    val wkndSe: String?,
    val lineNm: String?,
    val brlnNm: String?,
    val stnCd: String?,
    val stnNo: String?,
    val stnNm: String?,
    val dptreLineNm: String?,
    val dptreStnCd: String?,
    val dptreStnNm: String?,
    val dptreStnNo: String?,
    val arvlLineNm: String?,
    val arvlStnCd: String?,
    val arvlStnNm: String,
    val arvlStnNo: String?,
    val trainDptreTm: String?,
    val trainArvlTm: String?,
    val etrnYn: String?,
    val lnkgTrainno: String?,
    val tmprTmtblYn: String?,
    val vldBgngDt: String?,
    val vldEndDt: String?,
    val crtrYmd: String?
) {
    fun toDomain(finalStation: SubwayStation): SubwayTime? {
        return SubwayTime(
            isExpress = etrnYn == "Y",
            finalStation = finalStation,
            departureTime =
                TransitTimeParser.parseTime(trainDptreTm, LocalDate.now())
                    ?: return null,
            subwayDirection = SubwayDirection.fromName(upbdnbSe)
        )
    }
}
