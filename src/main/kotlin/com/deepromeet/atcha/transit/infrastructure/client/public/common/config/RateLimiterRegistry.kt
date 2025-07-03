package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger { }

@Component
class RateLimiterRegistry(props: OpenApiProps) {
    private val bucketMap = ConcurrentHashMap<String, Bucket>()
    private val limitMap = props.limits.perApi
    private val defaultLimit = props.limits.default

    fun awaitByUrl(
        requestUrl: String,
        urlKeyMap: Map<String, String>
    ) {
        val normalizedUrl = requestUrl.substringBefore('?')
        val key =
            urlKeyMap.entries.firstOrNull { (base, _) ->
                normalizedUrl.startsWith(base)
            }?.value ?: return

        val bucket =
            bucketMap.computeIfAbsent(normalizedUrl) {
                newBucket(limitMap[key] ?: defaultLimit)
            }

        while (!bucket.tryConsume(1)) {
            try {
                log.debug { "Rate limit exceeded for $normalizedUrl (key: $key), waiting..." }
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw kotlinx.coroutines.CancellationException("Rate‑limit wait interrupted", e)
            }
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
