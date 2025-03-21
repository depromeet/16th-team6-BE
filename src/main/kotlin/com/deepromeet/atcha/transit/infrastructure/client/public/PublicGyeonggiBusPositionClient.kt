package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PublicGyeonggiBusPositionClient(
    private val publicGyeonggiBusPositionClient: PublicGyeonggiBusPositionFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : BusPositionFetcher {
    override fun fetch(routeId: BusRouteId): List<BusPosition> {
        return publicGyeonggiBusPositionClient.getBusLocationList(serviceKey, routeId.value).let {
            it.response.msgBody.busLocationList.map { response ->
                response.toBusPosition()
            }
        }
    }
}
