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

    companion object {
        // DB normalized_name 컬럼과 동일한 규칙으로 정렬되어야 매칭이 일관됨.
        // 1) 괄호와 안의 내용 제거 (앞뒤 공백 포함)
        // 2) 끝의 '역' 접미사 제거
        // 3) 양 끝 공백 trim
        fun normalize(name: String): String {
            return name
                .replace(Regex(""" *\([^)]*\) *"""), "")
                .removeSuffix("역")
                .trim()
        }
    }
}
