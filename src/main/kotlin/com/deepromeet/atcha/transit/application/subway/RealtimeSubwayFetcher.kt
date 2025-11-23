package com.deepromeet.atcha.transit.application.subway

interface RealtimeSubwayFetcher {
    suspend fun fetch(statnNm: String)
}
