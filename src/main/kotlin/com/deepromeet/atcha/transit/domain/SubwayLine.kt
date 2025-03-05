package com.deepromeet.atcha.transit.domain

enum class SubwayLine(
    val routeName: String,
    val lnCd: String,
    val isCircular: Boolean = false
) {
    // 일반 노선
    LINE_1("1호선", "1"),
    LINE_2("2호선", "2", isCircular = true),
    LINE_3("3호선", "3"),
    LINE_4("4호선", "4"),
    LINE_5("5호선", "5"),
    LINE_6("6호선", "6"),
    LINE_7("7호선", "7"),
    LINE_8("8호선", "8"),
    LINE_9("9호선", "9"),

    // 광역철도
    SUIN_BUNDANG("수인분당선", "K1"),
    GYEONGUI_JUNGANG("경의중앙선", "K4"),
    GYEONGCHUN("경춘선", "K2"),
    GYEONGGANG("경강선", "K5"),
    AIRPORT("공항철도", "A1"),
    WEST_SEA("서해선", "WS"),

    // 신분당선
    SINBUNDANG("신분당선", "D1"),

    // 경전철
    U_LINE("의정부경전철", "U1"),
    EVERLINE("에버라인", "E1"),
    GIMPO_GOLDLINE("김포골드라인", "G1"),
    UI_SINSEOL("우이신설선", "UI"),

    // 인천 지하철
    INCHEON_1("인천 1호선", "I1"),
    INCHEON_2("인천 2호선", "I2"),

    // GTX 노선
    GTX_A("GTX-A", "A");

    companion object {
        fun fromRouteName(name: String): SubwayLine {
            return entries.find { it.routeName == name } ?: throw IllegalArgumentException("Invalid route name: $name")
        }
    }
}
