package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayTime
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.domain.TransitNameComparer
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.SubwayTimeResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicSubwayInfoClient(
    private val subwayInfoFeignClient: PublicSubwayInfoFeignClient,
    private val subwayStationRepository: SubwayStationRepository,
    private val transitNameComparer: TransitNameComparer,
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

    override suspend fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable {
        val (subwayStations, scheduleItems) = fetchDataConcurrently(startStation, dailyType, direction)
        val schedule = mapToSchedule(scheduleItems, subwayStations)
        return SubwayTimeTable(startStation, dailyType, direction, schedule)
    }

    private suspend fun fetchDataConcurrently(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): Pair<List<SubwayStation>, List<SubwayTimeResponse>> =
        coroutineScope {
            val stationsDeferred =
                async(Dispatchers.IO) {
                    subwayStationRepository.findByRouteCode(startStation.routeCode)
                }

            val scheduleDeferred =
                async(Dispatchers.IO) {
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
                            response.response.body.items?.item?.filter { it.endSubwayStationNm != null }
                                ?: throw TransitException.of(
                                    TransitError.NOT_FOUND_SUBWAY_SCHEDULE,
                                    "지하철 시간표 응답값이 NULL 입니다 - ${startStation.id} ${dailyType.code} ${direction.code}"
                                )
                        },
                        errorMessage =
                            "지하철 시간표 정보를 가져오는데 실패했습니다 -" +
                                " ${startStation.id} ${dailyType.code} ${direction.code}"
                    )
                }

            val subwayStations = stationsDeferred.await()
            val scheduleItems = scheduleDeferred.await()

            subwayStations to scheduleItems
        }

    private suspend fun mapToSchedule(
        scheduleItems: List<SubwayTimeResponse>,
        subwayStations: List<SubwayStation>
    ): List<SubwayTime> =
        coroutineScope {
            scheduleItems.map { item ->
                async(Dispatchers.IO) {
                    val finalStation =
                        subwayStations.find {
                                station ->
                            transitNameComparer.isSame(station.name, item.endSubwayStationNm)
                        }
                            ?: throw TransitException.of(
                                TransitError.NOT_FOUND_SUBWAY_STATION,
                                "지하철역 데이터에서 도착역 '${item.endSubwayStationNm}'을 찾을 수 없습니다."
                            )
                    item.toDomain(finalStation)
                }
            }.awaitAll().filterNotNull()
        }

    private fun <T> isSubwayApiLimitExceeded(response: PublicSubwayJsonResponse<T>): Boolean {
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
