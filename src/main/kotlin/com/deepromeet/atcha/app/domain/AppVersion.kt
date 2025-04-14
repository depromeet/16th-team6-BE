package com.deepromeet.atcha.app.domain

import com.deepromeet.atcha.common.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class AppVersion(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    val version: String,
    @Enumerated(EnumType.STRING)
    val platform: Platform
) : BaseTimeEntity() {
    override fun toString(): String {
        return "AppVersion(id=$id, version='$version', platform=$platform)"
    }
}
