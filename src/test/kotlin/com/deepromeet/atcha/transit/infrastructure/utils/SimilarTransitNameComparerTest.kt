package com.deepromeet.atcha.transit.infrastructure.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SimilarTransitNameComparerTest {
    private val comparer = SimilarTransitNameComparer()

    @Test
    fun `null이나 빈 문자열일 때 false를 반환한다`() {
        // null 케이스
        assertFalse(comparer.isSame(null, "강남역"))
        assertFalse(comparer.isSame("강남역", null))
        assertFalse(comparer.isSame(null, null))

        // 빈 문자열 케이스
        assertFalse(comparer.isSame("", "강남역"))
        assertFalse(comparer.isSame("강남역", ""))
        assertFalse(comparer.isSame("", ""))

        // 공백 문자열 케이스
        assertFalse(comparer.isSame("   ", "강남역"))
        assertFalse(comparer.isSame("강남역", "   "))
        assertFalse(comparer.isSame("   ", "   "))
    }

    @Test
    fun `동일한 문자열일 때 true를 반환한다`() {
        assertTrue(comparer.isSame("강남역", "강남역"))
        assertTrue(comparer.isSame("서울역", "서울역"))
        assertTrue(comparer.isSame("홍대입구역", "홍대입구역"))
    }

    @Test
    fun `포함하고 있는 문자열 일때 true를 반환한다`() {
        assertTrue(comparer.isSame("시청역9번출구.시청서소문2청사", "시청서소문2청사"))
    }

    @Test
    fun `정규화가 적용된 문자열이 동일할 때 true를 반환한다`() {
        assertTrue(comparer.isSame("강남역(지하)", "강남역"))
        assertTrue(comparer.isSame("서울역", "서울역(지하)"))
        assertTrue(comparer.isSame("홍대입구역(중)", "홍대입구역"))
        assertTrue(comparer.isSame("신촌역", "신촌역(중)"))
        assertTrue(comparer.isSame("강남역(지하)", "강남역(중)"))
    }

    @Test
    fun `공백이 포함된 문자열이 정규화 후 동일할 때 true를 반환한다`() {
        assertTrue(comparer.isSame("  강남역  ", "강남역"))
        assertTrue(comparer.isSame("강남역", "  강남역  "))
        assertTrue(comparer.isSame("  강남역(지하)  ", "  강남역  "))
    }

    @ParameterizedTest
    @CsvSource(
        "강남역, 강남, true",
        "홍대입구역, 홍대입구, true",
        "신촌역, 신촌, true",
        "서울역, 서울, true",
        "명동역, 명동, true",
        "강남역, 역삼역, false",
        "홍대입구역, 신촌역, false",
        "서울역, 용산역, false",
        "강남역, 완전다른역, false",
        "가, 나, false"
    )
    fun `유사도 기반 비교 테스트`(
        name1: String,
        name2: String,
        expected: Boolean
    ) {
        if (expected) {
            assertTrue(comparer.isSame(name1, name2))
        } else {
            assertFalse(comparer.isSame(name1, name2))
        }
    }

    @Test
    fun `유사하지만 임계값 이하인 문자열들은 false를 반환한다`() {
        assertFalse(comparer.isSame("강남역", "완전다른역명"))
        assertFalse(comparer.isSame("서울역", "제주시"))
        assertFalse(comparer.isSame("홍대입구역", "abc"))
    }
}
