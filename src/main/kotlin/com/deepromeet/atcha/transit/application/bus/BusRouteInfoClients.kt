package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

/**
 * 지역별 [BusRouteInfoClient] 를 골라 주는 단일 진입점.
 *
 * 기존에는 `Map<ServiceRegion, BusRouteInfoClient>` 를 여러 모듈(BusManager,
 * BusRouteResolver, BusRouteMatcher, RegionBusScheduleProvider)이 직접 주입받아
 * `map[region]!!` 으로 조회했다. 지원하지 않는 지역이 들어오면 의미 없는 NPE 가 발생했고,
 * "지역으로 클라이언트를 고른다" 라는 같은 로직이 다섯 군데에 흩어져 있었다.
 *
 * 이 모듈이 그 분기를 한곳으로 모으고, 미지원 지역에 대해 도메인 예외를 던진다.
 */
@Component
class BusRouteInfoClients(
    private val clientsByRegion: Map<ServiceRegion, BusRouteInfoClient>
) {
    fun forRegion(region: ServiceRegion): BusRouteInfoClient =
        clientsByRegion[region]
            ?: throw TransitException.of(
                TransitError.SERVICE_AREA_NOT_SUPPORTED,
                "지역 '${region.regionName}'의 버스 노선 정보를 제공하는 클라이언트가 없습니다."
            )
}
