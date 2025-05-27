package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

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
                isLimitExceeded = { response -> false },
                processResult = { response ->
                    response.result.station.find { it.arsID.trim() == station.busStationNumber.value.trim() }
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            ) ?: return null

        val busLanResponse =
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
        return busLanResponse.toBusArrival()
    }

    override fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo? {
        TODO("Not yet implemented")
    }
}
