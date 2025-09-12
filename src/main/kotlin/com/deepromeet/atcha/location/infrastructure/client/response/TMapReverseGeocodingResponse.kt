package com.deepromeet.atcha.location.infrastructure.client.response

import com.deepromeet.atcha.location.domain.ServiceRegion

data class TMapReverseGeocodingResponse(
    val addressInfo: AddressInfo
) {
    fun getFullAddress(): String {
        return addressInfo.fullAddress
    }

    data class AddressInfo(
        val city_do: String,
        val fullAddress: String
    ) {
        fun toServiceRegion(): ServiceRegion {
            return ServiceRegion.from(city_do)
        }
    }
}
