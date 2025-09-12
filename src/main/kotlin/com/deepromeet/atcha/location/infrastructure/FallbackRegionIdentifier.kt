package com.deepromeet.atcha.location.infrastructure

import com.deepromeet.atcha.location.application.RegionIdentifier
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.location.infrastructure.client.TMapRegionClient
import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class FallbackRegionIdentifier(
    private val localRegionIdentifier: LocalRegionIdentifier,
    private val tMapRegionClient: TMapRegionClient
) : RegionIdentifier {
    private val log = KotlinLogging.logger {}

    override suspend fun identify(coordinate: Coordinate): ServiceRegion {
        return try {
            localRegionIdentifier.identify(coordinate)
        } catch (e: TransitException) {
            throw e
        } catch (e: Exception) {
            log.warn(e) { "LocalRegionIdentifier 실패 - 폴백 시도" }
            tMapRegionClient.identify(coordinate)
        }
    }
}
