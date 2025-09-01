package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.application.TransitNameComparer
import com.deepromeet.atcha.transit.application.subway.SubwayTimetableClient
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwaySchedule
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse.Companion.isSubwayApiLimitExceeded
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.TrainScheduleResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.math.ceil

private const val PAGE_SIZE = 300

@Component
class PublicSubwayTimetableClient(
    private val subwayScheduleFeignClient: PublicSubwayScheduleFeignClient,
    private val subwayStationRepository: SubwayStationRepository,
    private val transitNameComparer: TransitNameComparer,
    @Value("\${open-api.api.service-key}") private val serviceKey: String,
    @Value("\${open-api.api.spare-key}") private val spareKey: String,
    @Value("\${open-api.api.real-last-key}") private val realLastKey: String
) : SubwayTimetableClient {
    override suspend fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable {
        val (stations, rawSchedules) = fetchDataConcurrently(startStation, dailyType, direction)
        val schedule = mapToSchedule(rawSchedules, stations)
        return SubwayTimeTable(startStation, dailyType, direction, schedule)
    }

    private suspend fun fetchDataConcurrently(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): Pair<List<SubwayStation>, List<TrainScheduleResponse>> =
        coroutineScope {
            val stationsDeferred =
                async(Dispatchers.IO) {
                    subwayStationRepository.findByRouteCode(startStation.routeCode)
                }

            val schedulesDeferred =
                async(Dispatchers.IO) {
                    fetchSchedulePages(startStation, dailyType, direction)
                }

            stationsDeferred.await() to schedulesDeferred.await()
        }

    private suspend fun fetchSchedulePages(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): List<TrainScheduleResponse> {
        val line = SubwayLine.fromCode(startStation.routeCode)

        val first =
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key ->
                    subwayScheduleFeignClient.getTrainSchedule(
                        key,
                        line.mainName(),
                        startStation.normalizeName(),
                        dailyType.description,
                        direction.getName(line.isCircular),
                        pageNo = 1,
                        numOfRows = PAGE_SIZE
                    )
                },
                isLimitExceeded = { resp -> isSubwayApiLimitExceeded(resp) },
                processResult = { it },
                errorMessage = "지하철 시간표 1페이지 조회 실패"
            )

        val totalCount = first.response.body.totalCount
        val totalPage = ceil(totalCount / PAGE_SIZE.toDouble()).toInt()

        val allItems = mutableListOf<TrainScheduleResponse>()
        first.response.body.items?.item?.let { allItems += it }

        if (totalPage > 1) {
            coroutineScope {
                (2..totalPage).map { page ->
                    async(Dispatchers.IO) {
                        ApiClientUtils.callApiWithRetry(
                            primaryKey = serviceKey,
                            spareKey = spareKey,
                            realLastKey = realLastKey,
                            apiCall = { key ->
                                subwayScheduleFeignClient.getTrainSchedule(
                                    key,
                                    line.mainName(),
                                    startStation.normalizeName(),
                                    dailyType.description,
                                    direction.getName(line.isCircular),
                                    pageNo = page,
                                    numOfRows = PAGE_SIZE
                                )
                            },
                            isLimitExceeded = { resp -> isSubwayApiLimitExceeded(resp) },
                            processResult = { resp ->
                                resp.response.body.items?.item ?: emptyList()
                            },
                            errorMessage = "지하철 시간표 page=$page 조회 실패"
                        )
                    }
                }.awaitAll().forEach { allItems += it }
            }
        }
        return allItems
    }

    private suspend fun mapToSchedule(
        scheduleItems: List<TrainScheduleResponse>,
        subwayStations: List<SubwayStation>
    ): List<SubwaySchedule> =
        coroutineScope {
            scheduleItems.map { item ->
                async(Dispatchers.Default) {
                    val finalStation =
                        subwayStations.find { st ->
                            transitNameComparer.isSame(st.name, item.arvlStnNm) ||
                                st.name.startsWith(item.arvlStnNm)
                        } ?: throw TransitException.of(
                            TransitError.NOT_FOUND_SUBWAY_STATION,
                            "${item.lineNm} 지하철역 데이터에서 도착역 '${item.arvlStnNm}'을 찾을 수 없습니다."
                        )
                    item.toDomain(finalStation)
                }
            }.awaitAll().filterNotNull()
        }
}
