package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.transit.domain.TimeDirection
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class LastRouteTimeAdjuster {
    fun adjustTransitDepartureTimes(legs: List<LastRouteLeg>): List<LastRouteLeg> {
        val adjustedLegs = legs.toMutableList()

        val transitLegs = adjustedLegs.withIndex().filter { it.value.isTransit() }
        if (transitLegs.isEmpty()) return adjustedLegs

        // 1. 대중교통 기준 가장 빠른 막차 시간 찾기
        val earliestTransitLeg =
            transitLegs.minBy {
                LocalDateTime.parse(it.value.departureDateTime!!)
            }

        var isAllRideable = true
        var lastUnrideableIndex: Int? = null

        // 2. 가장 빠른 출발 시간을 기준으로 뒤에 있는 대중교통 탑승 가능 여부 확인
        for (i in earliestTransitLeg.index until adjustedLegs.lastIndex) {
            val currentLeg = adjustedLegs[i]
            if (currentLeg.isWalk()) continue

            // 2-1. 출발 시간 + 소요 시간
            var currentLegAvailableTime =
                LocalDateTime.parse(currentLeg.departureDateTime!!)
                    .plusSeconds(currentLeg.sectionTime.toLong())

            // 2-2. 2-1 결과 시간과 다음 대중교통 출발 시간 비교 -> 탑승 가능 여부 확인
            var nextIndex = i + 1
            while (nextIndex <= adjustedLegs.lastIndex && adjustedLegs[nextIndex].isWalk()) {
                currentLegAvailableTime =
                    currentLegAvailableTime
                        .plusSeconds(adjustedLegs[nextIndex].sectionTime.toLong())
                nextIndex++
            }

            if (nextIndex > adjustedLegs.lastIndex) break

            val nextLeg = adjustedLegs[nextIndex]
            val nextLegDepartureTime = LocalDateTime.parse(nextLeg.departureDateTime!!)

            if (currentLegAvailableTime.isAfter(nextLegDepartureTime)) {
                isAllRideable = false
                lastUnrideableIndex = nextIndex
            }
        }

        // 3. 기준점 설정 : 가장 빠른 출발 시간 기준 or 탑승 불가한 마지막 대중교통
        val adjustBaseIndex = if (isAllRideable) earliestTransitLeg.index else lastUnrideableIndex!!

        // 4. 기준점 앞쪽 시간 재조정
        adjustLegsBeforeBase(adjustedLegs, adjustBaseIndex)

        // 5. 기준점 뒤쪽 시간 재조정
        adjustLegsAfterBase(adjustedLegs, adjustBaseIndex)

        return adjustedLegs
    }

    private fun adjustLegsBeforeBase(
        legs: MutableList<LastRouteLeg>,
        baseIndex: Int
    ) {
        var adjustBaseTime =
            legs[baseIndex].departureDateTime?.let {
                LocalDateTime.parse(it)
            }

        for (i in baseIndex - 1 downTo 0) {
            val leg = legs[i]

            if (adjustBaseTime == null) {
                legs[i] = leg.copy(departureDateTime = null)
                continue
            }

            if (leg.isWalk()) {
                adjustBaseTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
            val boardingTime = leg.calcBoardingTime(adjustedDepartureTime, TimeDirection.BEFORE)

            legs[i] =
                leg.copy(
                    departureDateTime =
                        boardingTime
                            .truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                )
            adjustBaseTime = boardingTime
        }
    }

    private fun adjustLegsAfterBase(
        legs: MutableList<LastRouteLeg>,
        baseIndex: Int
    ) {
        var adjustBaseTime =
            LocalDateTime.parse(legs[baseIndex].departureDateTime!!)
                .plusSeconds(legs[baseIndex].sectionTime.toLong())

        for (i in baseIndex + 1 until legs.size) {
            val leg = legs[i]

            if (adjustBaseTime == null) {
                legs[i] = leg.copy(departureDateTime = null)
                continue
            }

            if (leg.isWalk()) {
                adjustBaseTime = adjustBaseTime.plusSeconds(leg.sectionTime.toLong())
                continue
            }

            val boardingTime = leg.calcBoardingTime(adjustBaseTime, TimeDirection.AFTER)
            legs[i] =
                leg.copy(
                    departureDateTime =
                        boardingTime
                            .truncatedTo(ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                )
            adjustBaseTime = boardingTime.plusSeconds(leg.sectionTime.toLong())
        }
    }
}
