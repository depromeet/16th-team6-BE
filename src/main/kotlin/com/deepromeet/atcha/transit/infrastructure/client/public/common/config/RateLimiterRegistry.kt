package com.deepromeet.atcha.transit.infrastructure.client.public.common.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimiterRegistry(props: OpenApiProps) {
    private val bucketMap = ConcurrentHashMap<String, Bucket>()
    private val limitMap = props.limits.perApi
    private val defaultLimit = props.limits.default

    fun awaitByUrl(
        requestUrl: String,
        urlKeyMap: Map<String, String>
    ) {
        val key = urlKeyMap.entries.firstOrNull { (base, _) -> requestUrl.startsWith(base) }?.value ?: return
        val bucket = bucketMap.computeIfAbsent(key) { newBucket(limitMap[key] ?: defaultLimit) }
        while (!bucket.tryConsume(1)) Thread.sleep(100)
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
