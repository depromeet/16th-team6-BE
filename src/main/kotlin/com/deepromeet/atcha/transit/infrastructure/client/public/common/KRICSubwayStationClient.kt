package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.application.subway.SubwayStationFetcher
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayStationId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KRICSubwayStationClient(
    private val kricHttpClient: KRICSubwayStationHttpClient,
    private val publicSubwayInfoClient: PublicSubwayInfoClient,
    @Value("\${kric.api.service-key}")
    private val kricServiceKey: String
) : SubwayStationFetcher {
    override suspend fun fetch(lnCd: String): List<SubwayStation> =
        coroutineScope {
            val stationsResponse = kricHttpClient.getSubwayRouteInfo(kricServiceKey, lnCd).body
            stationsResponse.map { st ->
                async(Dispatchers.IO) {
                    val station = publicSubwayInfoClient.getSubwayStationByName(st.stinNm, st.routNm)
                    SubwayStation(
                        station?.id ?: SubwayStationId("UNKNOWN-" + st.stinCd),
                        st.stinCd,
                        st.stinNm,
                        st.routNm,
                        st.routCd
                    )
                }
            }.awaitAll()
        }
}
