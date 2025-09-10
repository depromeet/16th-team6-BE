package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.domain.subway.SubwayStation

interface SubwayStationFetcher {
    suspend fun fetch(lnCd: String): List<SubwayStation>
}
