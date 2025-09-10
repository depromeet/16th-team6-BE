package com.deepromeet.atcha.route.infrastructure.client.tmap

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.RouteItinerary
import com.deepromeet.atcha.route.infrastructure.client.tmap.mapper.toDomain
import com.deepromeet.atcha.route.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.shared.exception.ExternalApiError
import com.deepromeet.atcha.shared.exception.ExternalApiException
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import feign.RetryableException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Component
class TransitRouteClientV2(
    private val tmapRouteHttpClient: TMapRouteHttpClient
) {
    suspend fun fetchItinerariesV2(
        start: Coordinate,
        end: Coordinate
    ): List<RouteItinerary> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter) + "2300"

        val response =
            try {
                tmapRouteHttpClient.getRoutes(
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
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_TIME_OUT, e)
            } catch (e: CallNotPermittedException) {
                log.warn(e) { "외부 서비스 일시적 장애 발생" }
                throw ExternalApiException.of(ExternalApiError.EXTERNAL_API_CIRCUIT_BREAKER_OPEN)
            }

        response.result?.let { result ->
            when (result.status) {
                11 -> throw TransitException.of(TransitError.DISTANCE_TOO_SHORT)
                else -> throw TransitException.of(TransitError.SERVICE_AREA_NOT_SUPPORTED)
            }
        }

        val itineraries =
            response.metaData?.plan?.itineraries
                ?: throw TransitException.of(
                    TransitError.TRANSIT_API_ERROR,
                    "경로 검색 API에서 유효한 여행 경로를 반환하지 않았습니다."
                )

        return itineraries.map { it.toDomain() }
    }
}
