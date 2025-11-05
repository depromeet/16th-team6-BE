package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class PublicRateLimiterRegistry(props: OpenApiProps) {
    private val bucketMap = ConcurrentHashMap<String, Bucket>()
    private val limitMap = props.limits.perApi
    private val defaultLimit = props.limits.default

    suspend fun awaitByUrl(
        requestUrl: String,
        urlKeyMap: Map<String, String>
    ) {
        val normalizedUrl = requestUrl.substringBefore('?')
        val key =
            urlKeyMap.entries.firstOrNull { (base, _) ->
                normalizedUrl.startsWith(base)
            }?.value ?: return

        val limit = limitMap[key] ?: defaultLimit
        val bucket =
            bucketMap.computeIfAbsent(normalizedUrl) {
                newBucket(limit)
            }

        while (true) {
            val probe = bucket.tryConsumeAndReturnRemaining(1)
            if (probe.isConsumed) break
            val waitMillis = (probe.nanosToWaitForRefill / 1_000_000).coerceAtLeast(1)
            delay(waitMillis)
        }
    }

    private fun newBucket(permits: Int): Bucket =
        Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(permits.toLong())
                    .refillGreedy(permits.toLong(), Duration.ofSeconds(1))
                    .build()
            )
            .build()
}
