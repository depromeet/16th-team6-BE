package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.application.TransitNameComparer
import com.deepromeet.atcha.transit.application.subway.SubwayTimetableClient
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwaySchedule
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
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
    private val subwayScheduleHttpClient: PublicSubwayScheduleHttpClient,
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
        // 공공 API는 괄호 별칭/'역' 접미사를 뗀 정규화 이름으로 시간표를 색인한다.
        val primary = fetchSchedule(startStation, startStation.normalizedName, dailyType, direction)

        // 총신대입구(이수)처럼 노선별 역명이 다른 경우, 정규화 이름이 비면 괄호 안 별칭(이수)으로 재시도한다.
        val schedule =
            if (primary.isNotEmpty()) {
                primary
            } else {
                startStation.parenthesisAlias()
                    ?.let { fetchSchedule(startStation, it, dailyType, direction) }
                    ?: primary
            }

        return SubwayTimeTable(startStation, dailyType, direction, schedule)
    }

    private suspend fun fetchSchedule(
        startStation: SubwayStation,
        queryName: String,
        dailyType: DailyType,
        direction: SubwayDirection
    ): List<SubwaySchedule> {
        val (stations, rawSchedules) = fetchDataConcurrently(startStation, queryName, dailyType, direction)
        return mapToSchedule(rawSchedules, stations)
    }

    private suspend fun fetchDataConcurrently(
        startStation: SubwayStation,
        queryName: String,
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
                    fetchSchedulePages(startStation, queryName, dailyType, direction)
                }

            stationsDeferred.await() to schedulesDeferred.await()
        }

    private suspend fun fetchSchedulePages(
        startStation: SubwayStation,
        queryName: String,
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
                    subwayScheduleHttpClient.getTrainSchedule(
                        key,
                        line.mainName(),
                        queryName,
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
                                subwayScheduleHttpClient.getTrainSchedule(
                                    key,
                                    line.mainName(),
                                    queryName,
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
        // 공공 API는 stnNm을 부분 일치로 매칭하고('잠실' → 잠실/잠실나루/잠실새내) 정식 역명으로 응답한다('서울' → '서울역').
        // DB와 동일한 정규화 규칙으로 비교해 요청한 역에 해당하는 행만 남긴다.
        val target = SubwayStation.normalize(queryName)
        return allItems.filter { it.stnNm?.let { name -> SubwayStation.normalize(name) == target } == true }
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
                        } ?: SubwayStation(
                            id = null,
                            stationCode = item.stnCd ?: "UNKNOWN",
                            name = item.arvlStnNm,
                            routeName = SubwayLine.fromRouteName(item.lineNm!!).mainName(),
                            routeCode = SubwayLine.fromRouteName(item.lineNm).lnCd
                        )
                    item.toDomain(finalStation)
                }
            }.awaitAll().filterNotNull()
        }
}
