package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class SubwayStationBatchAppender(
    private val subwayStationRepository: SubwayStationRepository,
    private val subwayStationFetcher: SubwayStationFetcher
) {
    @Transactional
    fun appendAll() {
        if (subwayStationRepository.count() > 0) {
            return
        }

        val stationListsByLine =
            SubwayLine.entries.associate { line ->
                line to subwayStationFetcher.fetch(line.lnCd)
            }

        val allStations = stationListsByLine.values.flatten()
        subwayStationRepository.saveAll(allStations)
    }
}
