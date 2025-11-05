package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.application.TransitNameComparer
import com.deepromeet.atcha.transit.application.subway.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwaySchedule
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayStationData
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse.Companion.isSubwayApiLimitExceeded
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.SubwayTimeResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicSubwayInfoClient(
    private val subwayInfoHttpClient: PublicSubwayInfoHttpClient,
    private val subwayStationRepository: SubwayStationRepository,
    private val transitNameComparer: TransitNameComparer,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : SubwayInfoClient {
    suspend fun getSubwayStationByName(
        stationName: String,
        routeName: String
    ): SubwayStationData? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> subwayInfoHttpClient.getStationByName(key, stationName) },
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
                            subwayInfoHttpClient.getStationSchedule(
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
    ): List<SubwaySchedule> =
        coroutineScope {
            scheduleItems.map { item ->
                async(Dispatchers.Default) {
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
}
