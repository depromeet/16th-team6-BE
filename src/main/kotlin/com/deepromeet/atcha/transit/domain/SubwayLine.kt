package com.deepromeet.atcha.transit.domain

enum class SubwayLine(
    val routeName: List<String>,
    val lnCd: String,
    val isCircular: Boolean = false
) {
    // 일반 노선 (수도권호선)
    LINE_1(listOf("1호선", "수도권1호선(급행)", "수도권1호선"), "1"),
    LINE_2(listOf("2호선", "수도권2호선(급행)", "수도권2호선"), "2", isCircular = true),
    LINE_3(listOf("3호선", "수도권3호선(급행)", "수도권3호선"), "3"),
    LINE_4(listOf("4호선", "수도권4호선(급행)", "수도권4호선"), "4"),
    LINE_5(listOf("5호선", "수도권5호선(급행)", "수도권5호선"), "5"),
    LINE_6(listOf("6호선", "수도권6호선(급행)", "수도권6호선"), "6"),
    LINE_7(listOf("7호선", "수도권7호선(급행)", "수도권7호선"), "7"),
    LINE_8(listOf("8호선", "수도권8호선(급행)", "수도권8호선"), "8"),
    LINE_9(listOf("9호선", "수도권9호선(급행)", "수도권9호선"), "9"),

    // 광역철도
    SUIN_BUNDANG(listOf("수인분당선", "수인분당", "수인분당선(급행)"), "K1"),
    GYEONGUI_JUNGANG(listOf("경의선", "경의중앙선", "경의중앙"), "K4"),
    GYEONGCHUN(listOf("경춘선", "경춘"), "K2"),
    GYEONGGANG(listOf("경강선", "경강"), "K5"),
    AIRPORT(listOf("공항철도", "공항"), "A1"),
    WEST_SEA(listOf("서해선", "서해"), "WS"),
    SINLIM(listOf("신림선", "신림"), "L1"),

    // 신분당선 (원본 "신분당선"과 "신분당" 버전)
    SINBUNDANG(listOf("신분당선", "신분당"), "D1"),

    // 경전철
    U_LINE(listOf("의정부경전철"), "U1"),
    EVERLINE(listOf("용인경전철", "용인에버라인"), "E1"),
    GIMPO_GOLDLINE(listOf("김포도시철도", "김포골드라인"), "G1"),
    UI_SINSEOL(listOf("우이신설경전철", "우이신설선"), "UI"),

    // GTX 노선
    GTX_A(listOf("GTX-A"), "A"),

    // 인천
    INCHEON_LINE_1(listOf("인천선", "인천1호선", "인천1"), "I1"),
    INCHEON_LINE_2(listOf("인천2호선", "인천2"), "I2");

    fun mainName(): String {
        return routeName.first()
    }

    companion object {
        fun fromRouteName(name: String): SubwayLine {
            return entries.find { it.routeName.contains(name) }
                ?: throw IllegalArgumentException("Invalid route name: $name")
        }

        fun fromCode(code: String): SubwayLine {
            return entries.find { it.lnCd == code }
                ?: throw IllegalArgumentException("Invalid route code: $code")
        }
    }
}
