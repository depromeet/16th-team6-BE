package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.common.exception.InfrastructureError
import com.deepromeet.atcha.common.exception.InfrastructureException
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.infrastructure.client.public.ApiClientUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class ODSayBusInfoClient(
    private val oDSayBusFeignClient: ODSayBusFeignClient,
    @Value("\${odsay.api.service-key}")
    private val serviceKey: String,
    @Value("\${odsay.api.spare-key}")
    private val spareKey: String,
    @Value("\${odsay.api.real-last-key}")
    private val realLastKey: String,
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val ODSAY_API_CALL_COUNT_KEY = "odsay:call_count"
    }

    fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule? {
        val busStation =
            ApiClientUtils.callApiByKeyProvider(
                keyProvider = ::getApiKeyBasedOnUsage,
                apiCall = { key -> oDSayBusFeignClient.getStationByStationName(key, station.busStationMeta.name) },
                processResult = { response ->
                    response.result.station.find { it.arsID.trim() == station.busStationNumber.value.trim() }
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            ) ?: return null

        val busStationResponse =
            ApiClientUtils.callApiByKeyProvider(
                keyProvider = ::getApiKeyBasedOnUsage,
                apiCall = { key -> oDSayBusFeignClient.getStationInfoBystationID(key, busStation.stationID) },
                processResult = { response ->
                    response.result.lane.find { it.busLocalBlID == route.id.value }
                },
                errorMessage = "ODSay에서 정류장 정보를 가져오는데 실패했습니다."
            ) ?: return null

        return busStationResponse.toBusArrival(station)
    }

    private fun getApiKeyBasedOnUsage(): String {
        try {
            val count =
                redisTemplate.opsForValue().increment(ODSAY_API_CALL_COUNT_KEY, 1)
                    ?: throw IllegalStateException("Redis increment operation unexpectedly returned null.")

            return when {
                count <= 900 -> serviceKey
                count <= 1800 -> spareKey
                count <= 2700 -> realLastKey
                else -> {
                    throw InfrastructureException.of(
                        InfrastructureError.EXTERNAL_API_CALL_LIMIT_EXCEEDED,
                        "ODSay API 호출 제한을 초과했습니다. 현재 호출 횟수: $count"
                    )
                }
            }
        } catch (e: Exception) {
            log.warn(e) { "API 키 선택을 위한 Redis 작업 중 오류 발생" }
            throw InfrastructureException.of(InfrastructureError.CACHE_ERROR, cause = e)
        }
    }
}
