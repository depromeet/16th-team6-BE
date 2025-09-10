package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class SubwayStationBatchAppender(
    private val subwayStationRepository: SubwayStationRepository,
    private val subwayStationFetcher: SubwayStationFetcher
) {
    @Transactional
    suspend fun appendAll() {
        val stationListsByLine =
            SubwayLine.entries.associateWith { line -> subwayStationFetcher.fetch(line.lnCd) }

        val allStations = stationListsByLine.values.flatten()
        subwayStationRepository.saveAll(allStations)
    }
}
