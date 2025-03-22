package com.deepromeet.atcha.location.infrastructure.client.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI

data class TMapPOIResponse(
    val searchPoiInfo: SearchPoiInfo
) {
    fun toPOIs(): List<POI> {
        return searchPoiInfo.pois.poi.map { it.toPOI() }
    }

    data class SearchPoiInfo(
        val totalCount: String,
        val count: String,
        val page: String,
        val pois: TMapPOIs
    )

    data class TMapPOIs(
        val poi: List<TMapPOI>
    )

    data class TMapPOI(
        val name: String,
        val noorLat: String,
        val noorLon: String,
        val radius: String,
        val middleBizName: String,
        val lowerBizName: String,
        val newAddressList: NewAddressList
    ) {
        fun toPOI(): POI {
            return POI(
                Location(name, Coordinate(noorLat.toDouble(), noorLon.toDouble())),
                getBusinessCategory(),
                getLoadAddress(),
                radius.toDouble()
            )
        }

        private fun getBusinessCategory(): String {
            return "$middleBizName,$lowerBizName"
        }

        private fun getLoadAddress(): String {
            return newAddressList.newAddress[0].fullAddressRoad
        }
    }

    data class NewAddressList(
        val newAddress: List<NewAddress>
    )

    data class NewAddress(
        val fullAddressRoad: String
    )
}
