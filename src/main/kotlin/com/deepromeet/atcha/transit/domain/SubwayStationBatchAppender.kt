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
        val stationListsByLine =
            SubwayLine.entries.associateWith { line -> subwayStationFetcher.fetch(line.lnCd) }

        val allStations = stationListsByLine.values.flatten()
        subwayStationRepository.saveAll(allStations)
    }
}
