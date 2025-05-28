package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.DailyTypeResolver
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class PublicGyeonggiRouteInfoClient(
    private val publicGyeonggiRouteInfoFeignClient: PublicGyeonggiRouteInfoFeignClient,
    private val publicGyeonggiBusArrivalInfoFeignClient: PublicGyeonggiBusArrivalInfoFeignClient,
    private val dailyTypeResolver: DailyTypeResolver,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : BusRouteInfoClient {
    override fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival? =
        runBlocking {
            try {
                coroutineScope {
                    // 1. 노선 정보 가져오기 - 비동기
                    val busRouteInfoDeferred =
                        async(Dispatchers.IO) {
                            ApiClientUtils.callApiWithRetry(
                                primaryKey = serviceKey,
                                spareKey = spareKey,
                                realLastKey = realLastKey,
                                apiCall = {
                                        key ->
                                    publicGyeonggiRouteInfoFeignClient.getRouteInfo(key, route.id.value)
                                },
                                isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                                processResult = { response -> response.msgBody?.busRouteInfoItem },
                                errorMessage = "경기도 노선 정보 - ${route.name}-${route.id.value}를 가져오는데 실패했습니다."
                            )
                        }

                    // 2. 노선 정류장 목록 가져오기 - 비동기
                    val busRouteStationsDeferred =
                        async(Dispatchers.IO) {
                            ApiClientUtils.callApiWithRetry(
                                primaryKey = serviceKey,
                                spareKey = spareKey,
                                realLastKey = realLastKey,
                                apiCall = {
                                        key ->
                                    publicGyeonggiRouteInfoFeignClient.getRouteStationList(key, route.id.value)
                                },
                                isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                                processResult = { response -> response.msgBody },
                                errorMessage = "경기도 노선 정류장 목록 - ${route.name}-${route.id.value}을 가져오는데 실패했습니다."
                            )
                        }

                    // 결과 대기
                    val busRouteInfo = busRouteInfoDeferred.await() ?: return@coroutineScope null
                    val busRouteStations = busRouteStationsDeferred.await() ?: return@coroutineScope null

                    // 3. 정류장 정보 찾기
                    val stationInfo = busRouteStations.getStation(station.id)

                    // 4. 도착 정보 가져오기
                    val arrivalInfo =
                        ApiClientUtils.callApiWithRetry(
                            primaryKey = serviceKey,
                            spareKey = spareKey,
                            realLastKey = realLastKey,
                            apiCall = { key ->
                                publicGyeonggiBusArrivalInfoFeignClient.getArrivalInfo(
                                    key,
                                    station.id.value,
                                    route.id.value,
                                    stationInfo.stationSeq.toString()
                                )
                            },
                            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
                            processResult = { response -> response.msgBody?.busArrivalItem },
                            errorMessage = "경기도 버스 도착 정보를 가져오는데 실패했습니다."
                        ) ?: return@coroutineScope null

                    // 5. 버스 도착 정보 변환
                    busRouteInfo.toBusArrival(
                        dailyTypeResolver.resolve(),
                        stationInfo.getDirection(),
                        arrivalInfo,
                        busRouteStations
                    )
                }
            } catch (e: Exception) {
                log.warn(e) { "경기도 버스 도착 정보를 가져오는데 실패했습니다." }
                null
            }
        }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo? {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicGyeonggiRouteInfoFeignClient.getRouteInfo(key, route.id.value) },
            isLimitExceeded = { response -> ApiClientUtils.isGyeonggiApiLimitExceeded(response) },
            processResult = { response ->
                response.msgBody?.busRouteInfoItem?.toBusRouteOperationInfo()
            },
            errorMessage = "경기도 노선 운행 정보를 가져오는데 실패했습니다."
        )
    }
}
