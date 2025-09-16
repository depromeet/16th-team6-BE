package com.deepromeet.atcha.transit.infrastructure.cache.config

object CacheKeys {
    object Api {
        object Seoul {
            const val BUS_ROUTE_STATION_LIST = "api:seoul:busRouteStationList"
            const val BUS_ROUTE_LIST = "api:seoul:busRouteList"
        }

        object Incheon {
            const val BUS_ROUTE_STATION_LIST = "api:incheon:busRouteStationList"
            const val BUS_ROUTE_LIST = "api:incheon:busRouteList"
        }

        object Gyeonggi {
            const val BUS_ROUTE_STATION_LIST = "api:gyeonggi:busRouteStationList"
            const val BUS_ROUTE_LIST = "api:gyeonggi:busRouteList"
        }

        val BUS_ROUTE_STATION_LISTS =
            listOf(
                Seoul.BUS_ROUTE_STATION_LIST,
                Incheon.BUS_ROUTE_STATION_LIST,
                Gyeonggi.BUS_ROUTE_STATION_LIST
            )

        val BUS_ROUTE_LISTS =
            listOf(
                Seoul.BUS_ROUTE_LIST,
                Incheon.BUS_ROUTE_LIST,
                Gyeonggi.BUS_ROUTE_LIST
            )
    }

    object Transit {
        const val BUS_ROUTE_INFO = "transit:busRouteInfo"
    }
}
