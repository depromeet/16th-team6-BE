package com.deepromeet.atcha.location.api.response

import com.deepromeet.atcha.location.domain.POI
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class POIResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val businessCategory: String,
    val address: String,
    val radius: String?
) {
    companion object {
        fun from(domain: POI): POIResponse {
            return POIResponse(
                name = domain.location.name,
                lat = domain.location.coordinate.lat,
                lon = domain.location.coordinate.lon,
                businessCategory = domain.businessCategory,
                address = domain.address,
                radius = domain.radius?.let { radiusToString(it) }
            )
        }

        private fun radiusToString(radius: Double): String {
            if (radius < 1) {
                return "${(radius * 1000).toInt()}m"
            }
            return "${radius.toInt()}km"
        }
    }
}
