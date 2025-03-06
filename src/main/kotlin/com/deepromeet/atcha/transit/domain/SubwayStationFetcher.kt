package com.deepromeet.atcha.transit.domain

interface SubwayStationFetcher {
    fun fetch(lnCd: String): List<SubwayStation>
}
