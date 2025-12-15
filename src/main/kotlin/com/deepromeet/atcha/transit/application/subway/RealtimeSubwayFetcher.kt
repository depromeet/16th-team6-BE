package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayRealtimeResponse

interface RealtimeSubwayFetcher {
    suspend fun fetch(stationName: String): PublicSubwayRealtimeResponse
}
