package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.infrastructure.repository.SubwayStationRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class SubwayStationBatchAppender(
    private val subwayStationRepository: SubwayStationRepository,
    private val subwayStationFetcher: SubwayStationFetcher
) {
    @Transactional
    fun appendAll(): Unit =
        runBlocking {
            if (subwayStationRepository.count() > 0) {
                return@runBlocking
            }

            val stationListsByLine =
                SubwayLine.entries
                    .map { line ->
                        async(Dispatchers.IO) { line to subwayStationFetcher.fetch(line.lnCd) }
                    }
                    .awaitAll()
                    .toMap()

            val allStations = stationListsByLine.values.flatten()
            subwayStationRepository.saveAll(allStations)
        }
}
