package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

/**
 * 지역별 [BusPositionFetcher] 를 골라 주는 단일 진입점.
 *
 * [BusRouteInfoClients] 와 같은 이유로, `Map<ServiceRegion, BusPositionFetcher>` 직접 조회와
 * `map[region]!!` 를 대체한다.
 */
@Component
class BusPositionFetchers(
    private val fetchersByRegion: Map<ServiceRegion, BusPositionFetcher>
) {
    fun forRegion(region: ServiceRegion): BusPositionFetcher =
        fetchersByRegion[region]
            ?: throw TransitException.of(
                TransitError.SERVICE_AREA_NOT_SUPPORTED,
                "지역 '${region.regionName}'의 버스 위치 정보를 제공하는 클라이언트가 없습니다."
            )
}
