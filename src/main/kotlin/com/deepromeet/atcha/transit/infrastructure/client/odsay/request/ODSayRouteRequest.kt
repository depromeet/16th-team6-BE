package com.deepromeet.atcha.transit.infrastructure.client.odsay.request

data class ODSayRouteRequest(
    val startX: String,
    val startY: String,
    val endX: String,
    val endY: String,
    val count: Int,
    val searchDttm: String
)
