package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.location.domain.Coordinate

data class SuggestNotificationRequest(
    val lat: Double,
    val lon: Double
) {
    fun toCoordinate() = Coordinate(lat, lon)
}
