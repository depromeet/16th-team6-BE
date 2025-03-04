package com.deepromeet.atcha.user.domain

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
    val providerId: Long,
    var nickname: String,
    var profileImageUrl: String = "",
    @Embedded
    var address: Address = Address(),
    @Embedded
    var agreement: Agreement = Agreement(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_alert_frequencies", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "alert_frequencies")
    var alertFrequencies: MutableSet<Int> = mutableSetOf(),
    var isDeleted: Boolean = false
) {
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
            "nickname='$nickname', " +
            "profileImageUrl='$profileImageUrl', " +
            "address=$address, " +
            "agreement=$agreement, " +
            "alertFrequencies=$alertFrequencies, " +
            "isDeleted=$isDeleted)"
    }
}
