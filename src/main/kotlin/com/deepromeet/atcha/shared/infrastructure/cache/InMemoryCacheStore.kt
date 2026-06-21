package com.deepromeet.atcha.shared.infrastructure.cache

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Redis 없이 동작하는 [CacheStore] 어댑터. 테스트에서 도메인 캐시 어댑터를 끼워 검증할 때 쓴다.
 * TTL 은 검증 목적상 무시하고 단순 보관만 한다.
 */
class InMemoryCacheStore<V : Any> : CacheStore<V> {
    private val store = ConcurrentHashMap<String, V>()

    override fun get(key: String): V? = store[key]

    override fun put(
        key: String,
        value: V
    ) {
        store[key] = value
    }

    override fun put(
        key: String,
        value: V,
        ttl: Duration
    ) {
        store[key] = value
    }
}
