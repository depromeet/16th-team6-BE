package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayRealtimeResponse
import java.time.Duration
import java.time.LocalDateTime

enum class SubwayArrivalStatus {
    APPROACHING, // 진입 중
    ARRIVING, // 도착
    DEPARTED, // 출발
    OPERATING, // 운행 중
    WAITING // 대기
}

data class SubwayArrival(
    val trainNo: String,
    val arrivalStatus: SubwayArrivalStatus,
    val remainingTimeSeconds: Int,
    val remainingStations: Int?,
    val isLast: Boolean,
    val destination: String,
    val direction: String
) {
    val expectedArrivalTime: LocalDateTime
        get() = LocalDateTime.now().plusSeconds(remainingTimeSeconds.toLong())

    companion object {
        fun createScheduled(scheduledTime: LocalDateTime): SubwayArrival {
            val remainingSeconds =
                Duration.between(LocalDateTime.now(), scheduledTime)
                    .seconds.coerceAtLeast(0).toInt()

            return SubwayArrival(
                trainNo = "scheduled",
                arrivalStatus = SubwayArrivalStatus.WAITING,
                remainingTimeSeconds = remainingSeconds,
                remainingStations = null,
                isLast = false,
                destination = "",
                direction = ""
            )
        }

        fun fromRealtimeArrival(arrival: PublicSubwayRealtimeResponse.RealtimeArrival): SubwayArrival {
            return SubwayArrival(
                trainNo = arrival.btrainNo,
                arrivalStatus = parseArrivalStatus(arrival.arvlCd),
                remainingTimeSeconds = arrival.barvlDt.toIntOrNull() ?: 0,
                remainingStations = parseRemainingStations(arrival.arvlMsg3),
                isLast = arrival.lstcarAt == "1",
                destination = arrival.bstatnNm,
                direction = arrival.updnLine
            )
        }

        private fun parseArrivalStatus(arvlCd: String): SubwayArrivalStatus {
            return when (arvlCd) {
                "0" -> SubwayArrivalStatus.APPROACHING
                "1" -> SubwayArrivalStatus.ARRIVING
                "2" -> SubwayArrivalStatus.DEPARTED
                "3" -> SubwayArrivalStatus.OPERATING
                "4" -> SubwayArrivalStatus.OPERATING
                "5" -> SubwayArrivalStatus.OPERATING
                "99" -> SubwayArrivalStatus.OPERATING
                else -> SubwayArrivalStatus.OPERATING
            }
        }

        private fun parseRemainingStations(arvlMsg3: String): Int? {
            val regex = Regex("(\\d+)번째")
            return regex.find(arvlMsg3)?.groupValues?.get(1)?.toIntOrNull()
        }
    }
}
