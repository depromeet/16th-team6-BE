package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.domain.subway.Route
import com.deepromeet.atcha.transit.domain.subway.SubwayLine

interface SubwayRouteCache {
    fun get(subwayLine: SubwayLine): List<Route>?

    fun cache(
        subwayLine: SubwayLine,
        routes: List<Route>
    )
}
