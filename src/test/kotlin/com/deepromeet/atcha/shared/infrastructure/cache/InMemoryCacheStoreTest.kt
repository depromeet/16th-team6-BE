package com.deepromeet.atcha.shared.infrastructure.cache

import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryCacheStoreTest {
    private val store = InMemoryCacheStore<String>()

    @Test
    fun `저장 후 조회하면 값을 반환한다`() {
        store.put("k", "v")
        assertEquals("v", store.get("k"))
    }

    @Test
    fun `없는 키는 null 을 반환한다`() {
        assertNull(store.get("missing"))
    }

    @Test
    fun `같은 키에 다시 저장하면 덮어쓴다`() {
        store.put("k", "first")
        store.put("k", "second", Duration.ofMinutes(1))
        assertEquals("second", store.get("k"))
    }
}
