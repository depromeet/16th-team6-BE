package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayStationFetcher
import com.deepromeet.atcha.transit.domain.SubwayStationId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KRICSubwayStationClient(
    private val kricFeignClient: KRICSubwayStationFeignClient,
    private val publicFeignClient: PublicSubwayInfoFeignClient,
    @Value("\${kric.api.service-key}")
    private val kricServiceKey: String,
    @Value("\${open-api.api.service-key}")
    private val openApiServiceKey: String
) : SubwayStationFetcher {
    override suspend fun fetch(lnCd: String): List<SubwayStation> =
        coroutineScope {
            val stationsResponse = kricFeignClient.getSubwayRouteInfo(kricServiceKey, lnCd).body

            // 비동기 병렬 처리
            stationsResponse.map { st ->
                async(Dispatchers.IO) {
                    val station =
                        publicFeignClient.getStationByName(openApiServiceKey, st.stinNm)
                            .response
                            .body
                            .items
                            ?.item
                            ?.find { st.routNm == it.subwayRouteName }
                            ?.toData()

                    SubwayStation(
                        station?.id ?: SubwayStationId("UNKNOWN-" + st.stinCd),
                        st.stinCd,
                        st.stinNm,
                        st.routNm,
                        st.routCd,
                        st.stinConsOrdr
                    )
                }
            }.awaitAll() // 모든 비동기 요청 완료 후 리스트 반환
        }
}
