package com.deepromeet.atcha.transit.domain.subway

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class SubwayBranch(
    @Id
    val id: Long,
    val routeName: String,
    val routeCode: String,
    val ord: Int,
    val stationName: String,
    val finalStationName: String
)

data class Route(val list: List<SubwayBranch>) {
    private val stationMap by lazy { list.associateBy { it.stationName } }
    private val ordMap by lazy { list.associateBy { it.ord } }
    private val isCircular by lazy { list.firstOrNull()?.routeCode == "2" }

    fun isReachable(
        startStationName: String,
        endStationName: String,
        finalStationName: String,
        direction: SubwayDirection
    ): Boolean {
        val start = stationMap[startStationName] ?: return false
        val end = stationMap[endStationName] ?: return false
        val final = stationMap[finalStationName] ?: return false

        return if (isCircular) {
            isReachableInCircular(start.ord, end.ord, final.ord, direction)
        } else {
            // 기존 일반선 로직
            val minOrd = minOf(start.ord, final.ord)
            val maxOrd = maxOf(start.ord, final.ord)
            end.ord in minOrd..maxOrd
        }
    }

    fun isContains(
        startStationName: String,
        nextStationName: String,
        endStationName: String
    ): Boolean = startStationName in stationMap && nextStationName in stationMap && endStationName in stationMap

    fun getDirection(
        startStationName: String,
        nextStationName: String
    ): SubwayDirection {
        val start = stationMap[startStationName]!!
        val isLine9 = start.routeCode == "9"

        if (isCircular) {
            return getCircularDirection(start.ord, nextStationName)
        }

        val next = stationMap[nextStationName]!!
        val isDown = if (isLine9) start.ord > next.ord else start.ord < next.ord
        return if (isDown) SubwayDirection.DOWN else SubwayDirection.UP
    }

    private fun isReachableInCircular(
        startOrd: Int,
        endOrd: Int,
        finalOrd: Int,
        direction: SubwayDirection
    ): Boolean {
        return when (direction) {
            SubwayDirection.DOWN -> {
                // 외선: ord 증가 방향으로 이동
                if (startOrd <= finalOrd) {
                    // 정방향: startOrd -> finalOrd로 증가
                    endOrd in startOrd..finalOrd
                } else {
                    // 순환: startOrd -> 끝 -> 처음 -> finalOrd
                    endOrd >= startOrd || endOrd <= finalOrd
                }
            }
            SubwayDirection.UP -> {
                // 내선: ord 감소 방향으로 이동
                if (startOrd >= finalOrd) {
                    // 정방향: startOrd -> finalOrd로 감소
                    endOrd in finalOrd..startOrd
                } else {
                    // 순환: startOrd -> 처음 -> 끝 -> finalOrd
                    endOrd <= startOrd || endOrd >= finalOrd
                }
            }
        }
    }

    private fun getCircularDirection(
        startOrd: Int,
        nextStationName: String
    ): SubwayDirection {
        val maxOrd = list.maxOf { it.ord }

        // 실제 다음 역과 이전 역의 ord 계산
        val actualNextOrd = if (startOrd == maxOrd) 1 else startOrd + 1
        val actualPrevOrd = if (startOrd == 1) maxOrd else startOrd - 1

        // nextStationName이 실제 다음 역인지 이전 역인지 확인
        val actualNextStation = ordMap[actualNextOrd]?.stationName
        val actualPrevStation = ordMap[actualPrevOrd]?.stationName

        return when {
            nextStationName == actualNextStation -> SubwayDirection.DOWN
            nextStationName == actualPrevStation -> SubwayDirection.UP
            else -> throw IllegalStateException("연결되지 않은 역: $startOrd -> $nextStationName")
        }
    }
}
