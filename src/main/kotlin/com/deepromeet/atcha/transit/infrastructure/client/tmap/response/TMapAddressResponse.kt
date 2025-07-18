package com.deepromeet.atcha.transit.infrastructure.client.tmap.response

import com.deepromeet.atcha.transit.domain.region.ServiceRegion

data class TMapAddressResponse(
    val addressInfo: TMapAddressInfo
)

data class TMapAddressInfo(
    val city_do: String
) {
    fun toServiceRegion(): ServiceRegion {
        return ServiceRegion.from(city_do)
    }
}
