package com.deepromeet.atcha.auth.infrastructure.entity

import com.deepromeet.atcha.shared.infrastructure.jpa.BaseTimeEntity
import com.deepromeet.atcha.user.infrastructure.entity.UserEntity
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_provider")
class UserProviderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @OneToOne(fetch = FetchType.EAGER)
    val user: UserEntity,
    @Embedded
    val provider: ProviderEntity
) : BaseTimeEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserProviderEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "UserProviderEntity(id=$id, user=${user.id}, provider=$provider)"
    }
}
