package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ODSayBusInfoClient(
    private val oDSayBusFeignClient: ODSayBusFeignClient,
    @Value("\${odsay.api.service-key}")
    private val serviceKey: String,
    @Value("\${odsay.api.spare-key}")
    private val spareKey: String,
    @Value("\${odsay.api.real-last-key}")
    private val realLastKey: String
) : BusRouteInfoClient {
    override fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival? {
        val busStation =
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> oDSayBusFeignClient.getStationByStationName(key, station.busStationMeta.name) },
//            TODO : 여기 오류 났을 때 경우 추가하기
                isLimitExceeded = { response -> false },
                processResult = { response ->
                    response.result.station.find { it.arsID.equals(station.busStationNumber.value) }
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            ) ?: return null

        var busLanResponse =
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> oDSayBusFeignClient.getStationInfoBystationID(key, busStation.stationID) },
                isLimitExceeded = { response -> false },
                processResult = { response ->
                    response.result.lane.find { it.busLocalBlID == route.id.value }
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            ) ?: return null
        println("${busLanResponse.busNo} 버스 막차 시간 : ${busLanResponse.busLastTime}")

        return busLanResponse.toBusArrival()
    }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo? {
        TODO("Not yet implemented")
    }
}
