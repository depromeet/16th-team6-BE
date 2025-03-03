package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.common.BaseTimeEntity
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class UserProvider(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    @Embedded
    val provider: Provider
) : BaseTimeEntity() {
    constructor(userId: Long, provider: Provider) : this(id = 0, userId = userId, provider = provider)

    override fun toString(): String {
        return "ProviderToken(id=$id, userId=$userId, provider=$provider)"
    }
}
