package com.deepromeet.atcha.transit.config

import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicGyeonggiBusPositionClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicGyeonggiRouteInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicGyeonggiStationInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicSeoulBusPositionClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicSeoulBusRouteInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicSeoulBusStationInfoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BusBeanConfig(
    private val seoulStationInfoClient: PublicSeoulBusStationInfoClient,
    private val gyeonggiStationInfoClient: PublicGyeonggiStationInfoClient,
    private val seoulArrivalInfoClient: PublicSeoulBusRouteInfoClient,
    private val gyeonggiArrivalInfoClient: PublicGyeonggiRouteInfoClient,
    private val seoulPositionClient: PublicSeoulBusPositionClient,
    private val gyeonggiPositionClient: PublicGyeonggiBusPositionClient
) {
    @Bean
    fun stationInfoClient(): Map<ServiceRegion, BusStationInfoClient> =
        mapOf(
            ServiceRegion.SEOUL to seoulStationInfoClient,
            ServiceRegion.GYEONGGI to gyeonggiStationInfoClient
        )

    @Bean
    fun arrivalInfoClient(): Map<ServiceRegion, BusRouteInfoClient> =
        mapOf(
            ServiceRegion.SEOUL to seoulArrivalInfoClient,
            ServiceRegion.GYEONGGI to gyeonggiArrivalInfoClient
        )

    @Bean
    fun positionClient(): Map<ServiceRegion, BusPositionFetcher> =
        mapOf(
            ServiceRegion.SEOUL to seoulPositionClient,
            ServiceRegion.GYEONGGI to gyeonggiPositionClient
        )
}
