package com.deepromeet.atcha.transit.domain

data class BusRealTimeArrival(
    val realTimeInfoList: List<BusRealTimeInfo>
) {
    fun getSecondBus(): BusRealTimeInfo? = realTimeInfoList.getOrNull(1)
}
