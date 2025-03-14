package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.RealTimeBusArrival

data class RealTimeBusArrivalResponse(
    val realTimeBusArrival: List<RealTimeBusArrival>
)
