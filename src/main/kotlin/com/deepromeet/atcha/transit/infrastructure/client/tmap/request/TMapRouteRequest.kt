package com.deepromeet.atcha.transit.infrastructure.client.tmap.request

data class TMapRouteRequest(
    val startX: String,
    val startY: String,
    val endX: String,
    val endY: String,
    val count: Int,
    val searchDttm: String
)
