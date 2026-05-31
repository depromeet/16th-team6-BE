package com.deepromeet.atcha.transit.domain.subway

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class SubwayStation(
    @Id
    val id: SubwayStationId? = null,
    val stationCode: String,
    val name: String,
    val routeName: String,
    val routeCode: String,
    val normalizedName: String = normalize(name)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubwayStation) return false

        if (id != other.id) return false
        if (stationCode != other.stationCode) return false
        if (name != other.name) return false
        if (routeName != other.routeName) return false
        if (routeCode != other.routeCode) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    // 총신대입구(이수)처럼 노선별 역명이 다른 역은 공공 API가 괄호 안 별칭(이수)으로 시간표를 색인한다.
    fun parenthesisAlias(): String? {
        val match = Regex("""\(([^)]*)\)""").find(name) ?: return null
        return match.groupValues[1].trim().takeIf { it.isNotBlank() }
    }

    companion object {
        // DB normalized_name 컬럼과 동일한 규칙으로 정렬되어야 매칭이 일관됨.
        // trim 을 먼저 둬야 "신촌역 " 처럼 뒤 공백 있는 입력의 '역' 접미사 제거가 동작.
        fun normalize(name: String): String {
            return name
                .trim()
                .replace(Regex(""" *\([^)]*\) *"""), "")
                .removeSuffix("역")
        }
    }
}
