package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.shared.infrastructure.jpa.BaseTimeEntity
import com.deepromeet.atcha.user.domain.User
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class UserProvider(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @OneToOne(fetch = FetchType.EAGER)
    val user: User,
    @Embedded
    val provider: Provider
) : BaseTimeEntity() {
    constructor(user: User, provider: Provider) : this(id = 0, user = user, provider = provider)

    override fun toString(): String {
        return "UserProvider(id=$id, user=$user, provider=$provider)"
    }
}
