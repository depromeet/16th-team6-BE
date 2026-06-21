package com.deepromeet.atcha.shared.infrastructure.cache

import java.time.Duration

/**
 * 키-값 캐시의 단일 seam.
 *
 * 도메인 캐시 어댑터(SubwayRouteCache 등)는 이 인터페이스 뒤로 조회/저장을 위임하고,
 * Redis 예외 처리·히트 기록·get/set 배선 같은 반복 코드를 [com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheStore]
 * 한 곳에 모은다. 테스트는 [InMemoryCacheStore]를 끼워 Redis 없이 동작을 검증한다.
 */
interface CacheStore<V : Any> {
    fun get(key: String): V?

    fun put(
        key: String,
        value: V
    )

    fun put(
        key: String,
        value: V,
        ttl: Duration
    )
}
