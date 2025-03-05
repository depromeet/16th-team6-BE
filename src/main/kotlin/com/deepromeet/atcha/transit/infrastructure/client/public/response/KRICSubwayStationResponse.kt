package com.deepromeet.atcha.transit.infrastructure.client.public.response

data class PublicSubwayStationResponse(
    val body: List<KRICSubwayStationResponse>
)

data class KRICSubwayStationResponse(
    val mreaWideCd: String,
    val routCd: String,
    val routNm: String,
    val stinConsOrdr: Int,
    val railOprIsttCd: String,
    val lnCd: String,
    val stinCd: String,
    val stinNm: String
)
