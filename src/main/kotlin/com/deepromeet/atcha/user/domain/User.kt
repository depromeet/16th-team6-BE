package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.location.domain.Coordinate
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

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val providerId: String,
    @Embedded
    var address: Address = Address(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_alert_frequencies", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "alert_frequencies")
    var alertFrequencies: MutableSet<Int> = mutableSetOf(),
    var fcmToken: String,
    var isDeleted: Boolean = false
) {
    fun getHomeCoordinate(): Coordinate {
        return address.resolveCoordinate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "User(id=$id, " +
            "providerId=$providerId, " +
            "address=$address, " +
            "alertFrequencies=$alertFrequencies, " +
            "fcmToken='$fcmToken', " +
            "isDeleted=$isDeleted)"
    }
}
