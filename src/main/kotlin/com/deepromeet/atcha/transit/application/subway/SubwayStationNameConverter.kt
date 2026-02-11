package com.deepromeet.atcha.transit.application.subway

/**
 * 서울시 지하철 Open API 역 이름 변환 유틸리티
 *
 * 서울시 Open API는 일부 역에 대해 괄호를 포함한 전체 이름을 요구합니다.
 * DB에 저장된 역 이름은 normalizeName()으로 괄호가 제거되므로,
 * API 호출 시 다시 괄호를 포함한 이름으로 변환해야 합니다.
 */
object SubwayStationNameConverter {
    /**
     * 서울시 Open API에서 괄호를 포함한 전체 역 이름을 요구하는 역들의 매핑 테이블
     *
     * Key: 괄호가 제거된 역 이름 (normalizeName() 결과)
     * Value: 서울시 API에서 사용하는 전체 역 이름 (괄호 포함)
     */
    private val STATION_NAME_MAPPING = mapOf(
        "천호" to "천호(풍납토성)",
        "총신대입구" to "총신대입구(이수)",
        "충정로" to "충정로(경기대입구)",
        "남태령" to "남태령(사당)",
        "월곡" to "월곡(동덕여대)",
        "신설동" to "신설동(구버전)",
        "고속터미널" to "고속터미널(센트럴시티)"
    )

    /**
     * 서울시 Open API에서 사용하는 역 이름으로 변환
     *
     * @param normalizedName 괄호가 제거된 역 이름 (예: "천호")
     * @return 서울시 API에서 사용하는 역 이름 (예: "천호(풍납토성)")
     */
    fun convertToApiStationName(normalizedName: String): String {
        return STATION_NAME_MAPPING[normalizedName] ?: normalizedName
    }

    /**
     * 매핑 테이블에 등록된 역인지 확인
     */
    fun isMappedStation(normalizedName: String): Boolean {
        return STATION_NAME_MAPPING.containsKey(normalizedName)
    }

    /**
     * 현재 등록된 모든 매핑 정보 반환 (테스트/디버깅용)
     */
    fun getAllMappings(): Map<String, String> {
        return STATION_NAME_MAPPING.toMap()
    }
}
