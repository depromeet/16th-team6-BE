package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.domain.subway.SubwayStation

interface SubwayStationFetcher {
    fun fetch(lnCd: String): List<SubwayStation>
}
