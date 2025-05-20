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
class ODSaySeoulBusRouteInfoClient(
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
//        1. 정류장에 경유하는 노선(대중교통 정류장 검색)
//        v1/api/searchStation
//        결과 중 result > station > arsID
//        arsID == station.busStationNumber.value
//        정류장 하나 특정 완료
//            result > station > stationID
//
//        2. 버스정류장 세부정보 조회를 통해서 버스 하나 특정
//        busNo를 가져와서 BusRoute에서 비교해서 버스 노선(버스 하나)을 결정
//        result > lane > busLocalBlID == BusRoute.BusRouteId
//        busNo == BusRoute.name
        println("1111111111")
        // 버스 정류장 정보 가져오기
        var busStation =
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
        println("222222")

        // 막차 시간 가져오기
        var busLanResponse =
            ApiClientUtils.callApiWithRetry(
                primaryKey = serviceKey,
                spareKey = spareKey,
                realLastKey = realLastKey,
                apiCall = { key -> oDSayBusFeignClient.getStationInfoBystationID(key, busStation.stationID) },
//            TODO : 여기 오류 났을 때 경우 추가하기
                isLimitExceeded = { response -> false },
                processResult = { response ->
//                가져온 버스정류장 상세 정보에서 레인을 일치 시켜야함.
//                1. busNo와 route의 name을 비교해서
//                2. busNo은 중복될 수 있어서 busLocalBlID를 비교함
                    response.result.lane.find { it.busLocalBlID == route.id.value }
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            ) ?: return null
        println("3333333")

        println("완료!! __________________________")
        println("${busLanResponse.busNo} 버스 막차 시간 : ${busLanResponse.busLastTime}")

        return busLanResponse.toBusArrival()
    }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo? {
        TODO("Not yet implemented")
    }
}
