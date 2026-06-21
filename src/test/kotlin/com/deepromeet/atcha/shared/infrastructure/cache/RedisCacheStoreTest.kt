package com.deepromeet.atcha.shared.infrastructure.cache

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 예전엔 어댑터마다 복붙되던 try/catch + 히트 기록 정책을, 이제 한 곳([RedisCacheStore])에서 검증한다.
 */
class RedisCacheStoreTest {
    private val ops = mock<ValueOperations<String, String>>()
    private val template =
        mock<RedisTemplate<String, String>> {
            on { opsForValue() } doReturn ops
        }
    private val recorder = mock<RedisCacheHitRecorder>()

    @Test
    fun `값이 있으면 히트를 기록하고 값을 반환한다`() {
        whenever(ops.get("k")) doReturn "v"
        val store = RedisCacheStore(template, recorder, metric = "m")

        assertEquals("v", store.get("k"))
        verify(recorder).record("m", true)
    }

    @Test
    fun `값이 없으면 미스를 기록하고 null 을 반환한다`() {
        whenever(ops.get("k")) doReturn null
        val store = RedisCacheStore(template, recorder, metric = "m")

        assertNull(store.get("k"))
        verify(recorder).record("m", false)
    }

    @Test
    fun `조회가 실패하면 예외를 삼키고 미스로 처리한다`() {
        whenever(ops.get("k")) doThrow RuntimeException("redis down")
        val store = RedisCacheStore(template, recorder, metric = "m")

        assertNull(store.get("k"))
        verify(recorder).record("m", false)
    }

    @Test
    fun `metric 이 없으면 recorder 를 건드리지 않는다`() {
        whenever(ops.get("k")) doReturn "v"
        val store = RedisCacheStore(template, recorder, metric = null)

        assertEquals("v", store.get("k"))
        verify(recorder, never()).record(any(), any())
    }

    @Test
    fun `저장이 실패해도 예외를 던지지 않는다`() {
        doThrow(RuntimeException("redis down")).whenever(ops).set("k", "v")
        val store = RedisCacheStore(template, recorder)

        store.put("k", "v") // should not throw
    }
}
