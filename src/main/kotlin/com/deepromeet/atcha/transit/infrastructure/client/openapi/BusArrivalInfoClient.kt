package com.deepromeet.atcha.transit.infrastructure.client.openapi

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusArrivalInfoFetcher
import com.deepromeet.atcha.transit.domain.RouteId
import com.deepromeet.atcha.transit.domain.StationId
import org.springframework.stereotype.Component

@Component
class BusArrivalInfoClient(
    private val openAPIBusArrivalInfoFeignClient: OpenAPIBusArrivalInfoFeignClient
) : BusArrivalInfoFetcher {
    override fun getBusArrival(
        routeId: RouteId,
        stationId: StationId,
        order: Int
    ): BusArrival {
        val response =
            openAPIBusArrivalInfoFeignClient.getArrivalInfoByRoute(
                routeId.value.toString(),
                stationId.value.toString(),
                order
            )
        val result = response.msgBody.itemList[0]
        return result.toBusArrival()
    }
}
