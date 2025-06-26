package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayInfoClient
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationData
import com.deepromeet.atcha.transit.domain.SubwayTime
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicJsonResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.SubwayTimeResponse
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
    ): SubwayTimeTable? {
        return try {
            val (subwayStations, scheduleItems) =
                fetchDataConcurrently(startStation, dailyType, direction)
                    ?: return null

            val schedule = mapToSchedule(scheduleItems, subwayStations, startStation.name)

            SubwayTimeTable(startStation, dailyType, direction, schedule)
        } catch (e: TransitException) {
            log.warn(e) { "지하철 시간표 정보 처리 중 예상된 오류 발생: ${e.message}" }
            throw e
        } catch (e: Exception) {
            log.warn(e) { "지하철 시간표 정보 가져오는데 실패했습니다 - ${startStation.id} ${dailyType.code} ${direction.code}" }
            null
        }
    }

    private suspend fun fetchDataConcurrently(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): Pair<List<SubwayStation>, List<SubwayTimeResponse>>? =
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
                            response.response.body.items?.item?.filter { it.endSubwayStationNm != null } ?: emptyList()
                        },
                        errorMessage =
                            "지하철 시간표 정보를 가져오는데 실패했습니다 -" +
                                " ${startStation.id} ${dailyType.code} ${direction.code}"
                    )
                }

            val subwayStations = stationsDeferred.await()
            val scheduleItems = scheduleDeferred.await()

            if (scheduleItems == null) {
                return@coroutineScope null
            }

            subwayStations to scheduleItems
        }

    private suspend fun mapToSchedule(
        scheduleItems: List<SubwayTimeResponse>,
        subwayStations: List<SubwayStation>,
        startStationName: String
    ): List<SubwayTime> =
        coroutineScope {
            scheduleItems.map { item ->
                async(Dispatchers.IO) {
                    val finalStation =
                        subwayStations.find { station -> station.name == item.endSubwayStationNm }
                            ?: throw TransitException.of(
                                TransitError.NOT_FOUND_SUBWAY_STATION,
                                "지하철 '$startStationName'역의 시간표에서 도착역 '${item.endSubwayStationNm}'을 찾을 수 없습니다."
                            )
                    item.toDomain(finalStation)
                }
            }.awaitAll()
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
