package com.deepromeet.atcha.transit.infrastructure.client.public.common.response

/***
 * 지하철 실시간 도착 정보 응답 DTO
 * https://data.seoul.go.kr/dataList/OA-12764/F/1/datasetView.do
 */
data class PublicSubwayRealtimeResponse(
    val errorMessage: ErrorMessage,
    val realtimeArrivalList: List<RealtimeArrival>
) {
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
