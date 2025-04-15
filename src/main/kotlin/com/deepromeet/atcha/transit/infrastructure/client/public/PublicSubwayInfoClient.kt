package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicJsonResponse
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicSubwayInfoClient(
    private val subwayInfoFeignClient: PublicSubwayInfoFeignClient,
    private val subwayStationRepository: SubwayStationRepository,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : SubwayInfoClient {
    fun getSubwayStationByName(
        stationName: String,
        routeName: String
    ): SubwayStationData? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> subwayInfoFeignClient.getStationByName(key, stationName) },
            isLimitExceeded = { response -> isSubwayApiLimitExceeded(response) },
            processResult = { response ->
                response.response.body.items?.item
                    ?.find {
                        it.subwayRouteName == routeName && it.subwayStationName == stationName
                    }
                    ?.toData()
            },
            errorMessage = "지하철 역 정보를 가져오는데 실패했습니다 - $stationName, $routeName"
        )
    }

    override fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable? {
        return try {
            // 순차적으로 처리 (코루틴 병렬 처리 제거)
            val subwayStations = subwayStationRepository.findByRouteCode(startStation.routeCode)

            val scheduleItems =
                ApiClientUtils.callApiWithRetry(
                    primaryKey = serviceKey,
                    spareKey = spareKey,
                    realLastKey = realLastKey,
                    apiCall = { key ->
                        subwayInfoFeignClient.getStationSchedule(
                            key,
                            startStation.id!!.value,
                            dailyType.code,
                            direction.code
                        )
                    },
                    isLimitExceeded = { response -> isSubwayApiLimitExceeded(response) },
                    processResult = { response ->
                        response.response.body.items?.item
                            ?.filter { it.endSubwayStationNm != null }
                            ?: emptyList()
                    },
                    errorMessage =
                        "지하철 시간표 정보를 가져오는데 실패했습니다 " +
                            "- ${startStation.id} ${dailyType.code} ${direction.code}"
                ) ?: return null

            // 비동기 맵 대신 일반 맵 사용
            val timeTableItems =
                scheduleItems.map { item ->
                    val finalStation =
                        subwayStations.find { station -> station.name == item.endSubwayStationNm }
                            ?: throw TransitException.NotFoundSubwayStation
                    item.toDomain(finalStation)
                }

            SubwayTimeTable(
                startStation,
                dailyType,
                direction,
                timeTableItems
            )
        } catch (e: Exception) {
            log.warn(e) {
                "지하철 시간표 정보를 가져오는데 실패했습니다 - ${startStation.id} ${dailyType.code} ${direction.code}"
            }
            null
        }
    }

    private fun <T> isSubwayApiLimitExceeded(response: PublicJsonResponse<T>): Boolean {
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
