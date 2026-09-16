package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.application.bus.BusPositionFetchers
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import org.springframework.stereotype.Component

@Component
class CompositeBusPositionFetcher(
    private val busPositionFetchers: BusPositionFetchers
) {
    suspend fun fetch(busRoute: BusRoute): List<BusPosition> {
        if (busRoute.serviceRegion == ServiceRegion.SEOUL) {
            return try {
                val result = busPositionFetchers.forRegion(busRoute.serviceRegion).fetch(busRoute.id)
                if (result.isEmpty()) return busPositionFetchers.forRegion(ServiceRegion.GYEONGGI).fetch(busRoute.id)
                result
            } catch (e: Exception) {
                busPositionFetchers.forRegion(ServiceRegion.GYEONGGI).fetch(busRoute.id)
            }
        }

        return busPositionFetchers.forRegion(busRoute.serviceRegion).fetch(busRoute.id)
    }
}
