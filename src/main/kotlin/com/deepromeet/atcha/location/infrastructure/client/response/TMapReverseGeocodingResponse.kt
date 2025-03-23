package com.deepromeet.atcha.location.infrastructure.client.response

data class TMapReverseGeocodingResponse(
    val addressInfo: AddressInfo
) {
    fun getFullAddress(): String {
        return addressInfo.fullAddress
    }

    data class AddressInfo(
        val fullAddress: String
    )
}
