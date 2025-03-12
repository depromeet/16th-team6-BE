package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusArrivalInfoFetcher
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import org.springframework.stereotype.Component

@Component
class PublicSeoulBusArrivalInfoClient(
    private val publicSeoulBusArrivalInfoFeignClient: PublicSeoulBusArrivalInfoFeignClient
) : BusArrivalInfoFetcher {
    override fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival {
        val response =
            publicSeoulBusArrivalInfoFeignClient.getArrivalInfoByRoute(route.id.value)
        val findResult = response.msgBody.itemList?.find { it.arsId == station.id.value }
        requireNotNull(findResult) { "Bus arrival info not found" }
        return findResult.toBusArrival()
    }
}
