package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.application.bus.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import org.springframework.stereotype.Component

@Component
class CompositeBusPositionFetcher(
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>
) {
    suspend fun fetch(busRoute: BusRoute): List<BusPosition> {
        if (busRoute.serviceRegion == ServiceRegion.SEOUL) {
            return try {
                val result = busPositionFetcherMap[busRoute.serviceRegion]!!.fetch(busRoute.id)
                if (result.isEmpty()) return busPositionFetcherMap[ServiceRegion.GYEONGGI]!!.fetch(busRoute.id)
                result
            } catch (e: Exception) {
                busPositionFetcherMap[ServiceRegion.GYEONGGI]!!.fetch(busRoute.id)
            }
        }

        return busPositionFetcherMap[busRoute.serviceRegion]!!.fetch(busRoute.id)
    }
}
