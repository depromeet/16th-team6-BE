package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

const val AVERAGE_MINUTE_PER_STATION = 2

@JacksonXmlRootElement(localName = "response")
data class PublicGyeonggiResponse<T>(
    @field:JacksonXmlProperty(localName = "msgHeader")
    val msgHeader: GyeonggiMsgHeader = GyeonggiMsgHeader(),
    @field:JacksonXmlProperty(localName = "msgBody")
    val msgBody: T?
)

data class GyeonggiMsgHeader(
    @field:JacksonXmlProperty(localName = "queryTime")
    val queryTime: String = "",
    @field:JacksonXmlProperty(localName = "resultCode")
    val resultCode: String = "",
    @field:JacksonXmlProperty(localName = "resultMessage")
    val resultMessage: String = ""
) {
    fun isEmptyResponse(): Boolean {
        return resultCode == "4"
    }
}
