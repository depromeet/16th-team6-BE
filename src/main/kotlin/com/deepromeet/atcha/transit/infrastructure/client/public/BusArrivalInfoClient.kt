package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusArrivalInfoFetcher
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import org.springframework.stereotype.Component

@Component
class BusArrivalInfoClient(
    private val publicBusArrivalInfoFeignClient: PublicBusArrivalInfoFeignClient
) : BusArrivalInfoFetcher {
    override fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival? {
        val response =
            publicBusArrivalInfoFeignClient.getArrivalInfoByRoute(route.id.value)
        val findResult = response.msgBody.itemList?.find { it.arsId == station.arsId.value }
        return findResult?.toBusArrival()
    }
}
