package com.deepromeet.atcha.transit.domain

interface SubwayStationFetcher {
    suspend fun fetch(lnCd: String): List<SubwayStation>
}
