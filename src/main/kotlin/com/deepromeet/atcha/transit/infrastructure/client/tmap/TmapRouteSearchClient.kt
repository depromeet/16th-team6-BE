package com.deepromeet.atcha.transit.infrastructure.client.tmap

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.TransitRouteSearchClient
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import feign.RetryableException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Component
class TmapRouteSearchClient(
    private val tMapRouteClient: TMapRouteClient
) : TransitRouteSearchClient {
    override fun searchRoutes(
        start: Coordinate,
        end: Coordinate
    ): List<Itinerary> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter) + "2300"

        val response =
            try {
                tMapRouteClient.getRoutes(
                    TMapRouteRequest(
                        startX = start.lon.toString(),
                        startY = start.lat.toString(),
                        endX = end.lon.toString(),
                        endY = end.lat.toString(),
                        count = 20,
                        searchDttm = baseDate
                    )
                )
            } catch (e: RetryableException) {
                log.error(e) { "TMap API 호출 중 네트워크 오류(타임아웃 등) 발생" }
                throw TransitException.of(TransitError.API_TIME_OUT, e)
            }

        response.result?.let { result ->
            when (result.status) {
                11 -> throw TransitException.of(TransitError.DISTANCE_TOO_SHORT)
                else -> throw TransitException.of(TransitError.SERVICE_AREA_NOT_SUPPORTED)
            }
        }

        return response.metaData?.plan?.itineraries
            ?: throw TransitException.of(
                TransitError.TRANSIT_API_ERROR,
                "경로 검색 API에서 유효한 여행 경로를 반환하지 않았습니다."
            )
    }
}
