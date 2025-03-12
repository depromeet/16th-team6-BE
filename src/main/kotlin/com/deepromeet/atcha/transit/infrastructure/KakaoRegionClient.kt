package com.deepromeet.atcha.transit.infrastructure

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.RegionIdentifier
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.infrastructure.client.kakao.KakaoRegionFeignClient
import org.springframework.stereotype.Component

@Component
class KakaoRegionClient(
    private val kakaoRegionFeignClient: KakaoRegionFeignClient
) : RegionIdentifier {
    override fun identify(coordinate: Coordinate): ServiceRegion {
        val response = kakaoRegionFeignClient.getRegion(coordinate.lon.toString(), coordinate.lat.toString())
        return response.documents.first().toServiceRegion()
    }
}
