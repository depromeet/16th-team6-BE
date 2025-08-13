package com.deepromeet.atcha.location.infrastructure.client.response

import com.deepromeet.atcha.location.domain.ServiceRegion

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
