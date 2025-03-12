package com.deepromeet.atcha.transit.config

import com.deepromeet.atcha.transit.domain.BusArrivalInfoFetcher
import com.deepromeet.atcha.transit.domain.BusStationInfoClient
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicGyeonggiArrivalInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicGyeonggiStationInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicSeoulBusArrivalInfoClient
import com.deepromeet.atcha.transit.infrastructure.client.public.PublicSeoulBusStationInfoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BusBeanConfig(
    private val seoulStationInfoClient: PublicSeoulBusStationInfoClient,
    private val gyeonggiStationInfoClient: PublicGyeonggiStationInfoClient,
    private val seoulArrivalInfoClient: PublicSeoulBusArrivalInfoClient,
    private val gyeonggiArrivalInfoClient: PublicGyeonggiArrivalInfoClient
) {
    @Bean
    fun seoulStationInfoClient(): Map<ServiceRegion, BusStationInfoClient> =
        mapOf(
            ServiceRegion.SEOUL to seoulStationInfoClient,
            ServiceRegion.GYEONGGI to gyeonggiStationInfoClient
        )

    @Bean
    fun seoulArrivalInfoClient(): Map<ServiceRegion, BusArrivalInfoFetcher> =
        mapOf(
            ServiceRegion.SEOUL to seoulArrivalInfoClient,
            ServiceRegion.GYEONGGI to gyeonggiArrivalInfoClient
        )
}
