package com.deepromeet.atcha.transit.domain.subway

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubwayStationTest {
    @ParameterizedTest
    @CsvSource(
        "삼성(무역센터), 삼성",
        "총신대입구(이수), 총신대입구",
        "교대(법원·검찰청), 교대",
        "서울역, 서울",
        "강남, 강남"
    )
    fun `normalize는 괄호 별칭과 역 접미사를 제거한다`(
        name: String,
        expected: String
    ) {
        assertEquals(expected, SubwayStation.normalize(name))
    }

    @ParameterizedTest
    @CsvSource(
        "총신대입구(이수), 이수",
        "삼성(무역센터), 무역센터",
        "교대(법원·검찰청), 법원·검찰청"
    )
    fun `parenthesisAlias는 괄호 안 별칭을 추출한다`(
        name: String,
        expected: String
    ) {
        assertEquals(expected, station(name).parenthesisAlias())
    }

    @ParameterizedTest
    @ValueSource(strings = ["강남", "서울역", "역삼"])
    fun `괄호가 없으면 parenthesisAlias는 null이다`(name: String) {
        assertNull(station(name).parenthesisAlias())
    }

    private fun station(name: String) =
        SubwayStation(
            id = null,
            stationCode = "0000",
            name = name,
            routeName = "2호선",
            routeCode = "2"
        )
}
