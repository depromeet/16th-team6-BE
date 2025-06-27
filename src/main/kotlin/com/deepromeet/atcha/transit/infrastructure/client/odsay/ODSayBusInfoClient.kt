package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils
import org.springframework.stereotype.Component

@Component
class ODSayBusInfoClient(
    private val oDSayBusFeignClient: ODSayBusFeignClient,
    private val oDSayCallCounter: ODSayCallCounter
) {
    fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule {
        val busStation =
            ApiClientUtils.callApiByKeyProvider(
                keyProvider = oDSayCallCounter::getApiKeyBasedOnUsage,
                apiCall = { key -> oDSayBusFeignClient.getStationByStationName(key, station.busStationMeta.name) },
                processResult = { response ->
                    response.result.station.find { it.arsID.trim() == station.busStationNumber.value.trim() }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_STATION,
                            "ODSay에서 정류장 '${station.busStationMeta.name}'을 찾을 수 없습니다."
                        )
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            )

        val busStationResponse =
            ApiClientUtils.callApiByKeyProvider(
                keyProvider = oDSayCallCounter::getApiKeyBasedOnUsage,
                apiCall = { key -> oDSayBusFeignClient.getStationInfoBystationID(key, busStation.stationID) },
                processResult = { response ->
                    response.result.lane.find { it.busLocalBlID == route.id.value }
                        ?: throw TransitException.of(
                            TransitError.NOT_FOUND_BUS_ROUTE,
                            "ODSay에서 노선 '${route.name} - ${route.id.value}'을 찾을 수 없습니다."
                        )
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            )

        return busStationResponse.toBusArrival(station)
    }
}
