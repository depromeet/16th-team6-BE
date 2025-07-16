package com.deepromeet.atcha.route.domain

data class RouteStep(
    val distance: Double,
    val streetName: String?,
    val description: String?,
    val linestring: String?
)
