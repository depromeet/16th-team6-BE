package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.application.subway.SubwayStationMeta

data class SubwayStationData(
    val id: SubwayStationId,
    val info: SubwayStationMeta
)
