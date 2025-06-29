package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusRouteInfo
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.common.utils.ApiClientUtils
import org.springframework.stereotype.Component

@Component
class ODSayBusInfoClient(
    private val oDSayBusFeignClient: ODSayBusFeignClient,
    private val oDSayCallCounter: ODSayCallCounter
) {
    fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule {
        val busStation =
            ApiClientUtils.callApiByKeyProvider(
                keyProvider = oDSayCallCounter::getApiKeyBasedOnUsage,
                apiCall = {
                        key ->
                    oDSayBusFeignClient.getStationByStationName(
                        key,
                        routeInfo.getTargetStation().stationName
                    )
                },
                processResult = { response ->
                    response.result.station
                        .find { it.arsID.trim() == routeInfo.getTargetStation().stationNumber.trim() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_STATION,
                            "ODSay에서 정류장 '${routeInfo.getTargetStation().stationName}'을 찾을 수 없습니다."
                        )
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            )

        val busStationResponse =
            ApiClientUtils.callApiByKeyProvider(
                keyProvider = oDSayCallCounter::getApiKeyBasedOnUsage,
                apiCall = { key -> oDSayBusFeignClient.getStationInfoByStationID(key, busStation.stationID) },
                processResult = { response ->
                    response.result.lane.find { it.busLocalBlID == routeInfo.routeId }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "ODSay에서 노선 '${routeInfo.route.name} - ${routeInfo.routeId}'을 찾을 수 없습니다."
                        )
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            )

        return busStationResponse.toBusSchedule(routeInfo.getTargetStation().busStation)
    }
}
