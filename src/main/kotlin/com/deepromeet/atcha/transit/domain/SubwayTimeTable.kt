package com.deepromeet.atcha.transit.domain

data class SubwayTimeTable(
    val startStation: SubwayStation,
    val dailyType: DailyType,
    val subwayDirection: SubwayDirection,
    val schedule: List<SubwayTime>
) {
    fun getLastTime(endStation: SubwayStation): SubwayTime? =
        schedule
            .filter {
                if (subwayDirection == SubwayDirection.DOWN) {
                    it.finalStation.ord >= endStation.ord
                } else {
                    it.finalStation.ord <= endStation.ord
                }
            }
            .maxByOrNull { it.arrivalTime }
}
