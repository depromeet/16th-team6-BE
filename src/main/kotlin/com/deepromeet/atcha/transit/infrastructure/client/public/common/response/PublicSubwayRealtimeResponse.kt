package com.deepromeet.atcha.transit.infrastructure.client.public.common.response

/***
 * 지하철 실시간 도착 정보 응답 DTO
 * https://data.seoul.go.kr/dataList/OA-12764/F/1/datasetView.do
 *
 * 주의: API는 성공 시와 오류 시 다른 JSON 구조를 반환합니다.
 * - 성공: { "errorMessage": {...}, "realtimeArrivalList": [...] }
 * - 오류: { "status": 500, "code": "...", "message": "...", ... } (ErrorMessage 구조)
 */
data class PublicSubwayRealtimeResponse(
    val errorMessage: ErrorMessage? = null,
    val realtimeArrivalList: List<RealtimeArrival>? = null,
    // 오류 응답을 위한 필드 (ErrorMessage와 동일한 구조)
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
    val link: String? = null,
    val developerMessage: String? = null,
    val total: Int? = null
) {
    /**
     * 오류 메시지를 반환합니다.
     * - errorMessage 필드가 있으면 해당 필드 반환
     * - 없으면 루트 레벨의 오류 필드로 ErrorMessage 생성
     */
    fun resolveErrorMessage(): ErrorMessage {
        return errorMessage ?: ErrorMessage(
            status = status ?: 500,
            code = code ?: "UNKNOWN",
            message = message ?: "Unknown error",
            link = link ?: "",
            developerMessage = developerMessage ?: "",
            total = total ?: 0
        )
    }

    data class ErrorMessage(
        val status: Int,
        val code: String,
        val message: String,
        val link: String,
        val developerMessage: String,
        val total: Int
    )

    data class RealtimeArrival(
        val arvlCd: String,
        val arvlMsg2: String,
        val arvlMsg3: String,
        val barvlDt: String,
        val beginRow: Int?,
        val bstatnId: String,
        val bstatnNm: String,
        val btrainNo: String,
        val btrainSttus: String,
        val curPage: Int?,
        val endRow: Int?,
        val lstcarAt: String,
        val ordkey: String,
        val pageRow: Int?,
        val recptnDt: String,
        val rowNum: Int,
        val selectedCount: Int,
        val statnFid: String,
        val statnId: String,
        val statnList: String,
        val statnNm: String,
        val statnTid: String,
        val subwayHeading: String?,
        val subwayId: String,
        val subwayList: String,
        val subwayNm: String?,
        val totalCount: Int,
        val trainCo: String?,
        val trainLineNm: String,
        val trnsitCo: String,
        val updnLine: String
    )
}
