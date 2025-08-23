package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

@Component
class PublicRateLimiterRegistry(props: OpenApiProps) {
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
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw CancellationException()
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
