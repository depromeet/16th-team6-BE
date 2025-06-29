package com.deepromeet.atcha.transit.infrastructure.client.public.common.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDate

@JacksonXmlRootElement(localName = "response")
data class HolidayResponse(
    @field:JacksonXmlProperty(localName = "header")
    val header: Header,
    @field:JacksonXmlProperty(localName = "body")
    val body: Body
) {
    data class Header(
        @field:JacksonXmlProperty(localName = "resultCode")
        val resultCode: String,
        @field:JacksonXmlProperty(localName = "resultMsg")
        val resultMsg: String
    )

    data class Body(
        @field:JacksonXmlProperty(localName = "items")
        val items: Items,
        @field:JacksonXmlProperty(localName = "numOfRows")
        val numOfRows: Int,
        @field:JacksonXmlProperty(localName = "pageNo")
        val pageNo: Int,
        @field:JacksonXmlProperty(localName = "totalCount")
        val totalCount: Int
    ) {
        data class Items(
            @JacksonXmlElementWrapper(useWrapping = false)
            @field:JacksonXmlProperty(localName = "item")
            val item: List<Item>
        ) {
            data class Item(
                @field:JacksonXmlProperty(localName = "dateKind")
                val dateKind: String,
                @field:JacksonXmlProperty(localName = "dateName")
                val dateName: String,
                @field:JacksonXmlProperty(localName = "isHoliday")
                val isHoliday: String,
                @field:JacksonXmlProperty(localName = "locdate")
                val locdate: Long,
                @field:JacksonXmlProperty(localName = "seq")
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
        }
    }
}
