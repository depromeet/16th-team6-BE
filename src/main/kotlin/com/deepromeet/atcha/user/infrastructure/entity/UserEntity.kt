package com.deepromeet.atcha.user.infrastructure.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

/**
 * User JPA Entity - Infrastructure Layer
 * 데이터베이스 저장을 위한 기술적 구현체
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "provider_id", nullable = false)
    val providerId: String,
    @Embedded
    var address: AddressEntity = AddressEntity(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_alert_frequencies", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "alert_frequencies")
    var alertFrequencies: MutableSet<Int> = mutableSetOf(),
    @Column(name = "fcm_token")
    var fcmToken: String? = null,
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "UserEntity(id=$id, " +
            "providerId='$providerId', " +
            "address=$address, " +
            "alertFrequencies=$alertFrequencies, " +
            "fcmToken='$fcmToken', " +
            "isDeleted=$isDeleted)"
    }
}
