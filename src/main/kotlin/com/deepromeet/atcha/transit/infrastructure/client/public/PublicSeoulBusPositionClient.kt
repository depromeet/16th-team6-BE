package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicSeoulBusPositionClient(
    private val publicSeoulBusPositionClient: PublicSeoulBusPositionFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : BusPositionFetcher {
    override fun fetch(routeId: BusRouteId): List<BusPosition> {
        return publicSeoulBusPositionClient.getBusPosByRtid(serviceKey, routeId.value).let {
            it.msgBody.itemList?.map {
                    busPositionResponse ->
                busPositionResponse.toBusPosition()
            }
        } ?: emptyList()
    }
}
