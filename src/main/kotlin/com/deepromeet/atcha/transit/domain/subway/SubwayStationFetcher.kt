package com.deepromeet.atcha.transit.domain.subway

interface SubwayStationFetcher {
    fun fetch(lnCd: String): List<SubwayStation>
}
