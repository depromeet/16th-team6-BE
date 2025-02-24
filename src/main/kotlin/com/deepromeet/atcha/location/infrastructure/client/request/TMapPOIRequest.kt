package com.deepromeet.atcha.location.infrastructure.client.request

data class TMapPOIRequest(
    val searchKeyword: String,
    val centerLat: Double,
    val centerLon: Double,
    val page: Int,
    val count: Int,
    val appKey: String,
    val version: String = "1"
)
