package com.deepromeet.atcha.transit.config

import com.deepromeet.atcha.transit.domain.BusPositionFetcher
import com.deepromeet.atcha.transit.domain.BusRouteInfoClient
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.PublicGyeonggiBusPositionClient
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.PublicGyeonggiRouteInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.PublicIncheonBusPositionClient
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.PublicIncheonRouteInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.PublicSeoulBusPositionClient
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.PublicSeoulBusRouteInfoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BusBeanConfig(
    private val seoulRouteInfoClient: PublicSeoulBusRouteInfoClient,
    private val seoulPositionClient: PublicSeoulBusPositionClient,
    private val gyeonggiRouteInfoClient: PublicGyeonggiRouteInfoClient,
    private val gyeonggiPositionClient: PublicGyeonggiBusPositionClient,
    private val incheonRouteInfoClient: PublicIncheonRouteInfoClient,
    private val incheonPositionClient: PublicIncheonBusPositionClient
) {
    @Bean
    fun routeInfoClient(): Map<ServiceRegion, BusRouteInfoClient> =
        mapOf(
            ServiceRegion.SEOUL to seoulRouteInfoClient,
            ServiceRegion.GYEONGGI to gyeonggiRouteInfoClient,
            ServiceRegion.INCHEON to incheonRouteInfoClient
        )

    @Bean
    fun positionClient(): Map<ServiceRegion, BusPositionFetcher> =
        mapOf(
            ServiceRegion.SEOUL to seoulPositionClient,
            ServiceRegion.GYEONGGI to gyeonggiPositionClient,
            ServiceRegion.INCHEON to incheonPositionClient
        )
}
