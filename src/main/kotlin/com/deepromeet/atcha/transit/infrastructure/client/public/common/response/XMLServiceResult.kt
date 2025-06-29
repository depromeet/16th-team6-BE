package com.deepromeet.atcha.transit.infrastructure.client.public.common.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "ServiceResult")
data class ServiceResult<T>(
    @JacksonXmlProperty(localName = "msgHeader")
    val msgHeader: MsgHeader,
    @JacksonXmlProperty(localName = "msgBody")
    val msgBody: MsgBody<T>
)

data class MsgHeader(
    @JacksonXmlProperty(localName = "headerCd")
    val headerCd: Int?,
    @JacksonXmlProperty(localName = "headerMsg")
    val headerMsg: String?,
    @JacksonXmlProperty(localName = "itemCount")
    val itemCount: Int?,
    @JacksonXmlProperty(localName = "numOfRows")
    val numOfRows: Int?,
    @JacksonXmlProperty(localName = "pageNo")
    val pageNo: Int?,
    @JacksonXmlProperty(localName = "resultCode")
    val resultCode: Int?,
    @JacksonXmlProperty(localName = "resultMsg")
    val resultMessage: String?,
    @JacksonXmlProperty(localName = "totalCount")
    val totalCount: Int?
)

data class MsgBody<T>(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "itemList")
    val itemList: List<T>?
)
