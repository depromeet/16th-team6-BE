package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDate

@JacksonXmlRootElement(localName = "response")
data class PublicXMLResponse<T>(
    @JacksonXmlProperty(localName = "header")
    val header: ResponseHeader,
    @JacksonXmlProperty(localName = "body")
    val body: ResponseBody<T>
)

data class ResponseHeader(
    @JacksonXmlProperty(localName = "resultCode")
    val resultCode: String,
    @JacksonXmlProperty(localName = "resultMsg")
    val resultMsg: String
)

data class ResponseBody<T>(
    @JacksonXmlElementWrapper(localName = "items", useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val items: List<T>?,
    @JacksonXmlProperty(localName = "numOfRows")
    val numOfRows: Int,
    @JacksonXmlProperty(localName = "pageNo")
    val pageNo: Int,
    @JacksonXmlProperty(localName = "totalCount")
    val totalCount: Int
)

data class HolidayResponse(
    @JacksonXmlProperty(localName = "dateKind")
    val dateKind: String,
    @JacksonXmlProperty(localName = "dateName")
    val dateName: String,
    @JacksonXmlProperty(localName = "isHoliday")
    val isHoliday: String,
    @JacksonXmlProperty(localName = "locdate")
    val locdate: Int,
    @JacksonXmlProperty(localName = "seq")
    val seq: Int
) {
    fun toLocalDate(): LocalDate {
        val date = locdate.toString()
        val year = date.substring(0, 4).toInt()
        val month = date.substring(4, 6).toInt()
        val day = date.substring(6, 8).toInt()
        return LocalDate.of(year, month, day)
    }
}
