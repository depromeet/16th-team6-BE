package com.deepromeet.atcha.transit.infrastructure.client.response

import com.deepromeet.atcha.transit.domain.Plan

data class TMapRouteResponse(
    val metaData: MetaData,
)

data class MetaData(
    val plan: Plan,
)
