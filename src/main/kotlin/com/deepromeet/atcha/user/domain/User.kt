package com.deepromeet.atcha.user.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val clientId: Long,
    var nickname: String,
    var profileImageUrl: String = "",
    var address: String = "",
    var addressLat: Double = 0.0,
    var addressLog: Double? = 0.0,
    var alertAgreement: Boolean = true,
    var trackingAgreement: Boolean = true,
    var isDeleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (clientId != other.clientId) return false
        if (nickname != other.nickname) return false
        if (profileImageUrl != other.profileImageUrl) return false
        if (address != other.address) return false
        if (addressLat != other.addressLat) return false
        if (addressLog != other.addressLog) return false
        if (alertAgreement != other.alertAgreement) return false
        if (trackingAgreement != other.trackingAgreement) return false
        if (isDeleted != other.isDeleted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + clientId.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + profileImageUrl.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + addressLat.hashCode()
        result = 31 * result + addressLog.hashCode()
        result = 31 * result + alertAgreement.hashCode()
        result = 31 * result + trackingAgreement.hashCode()
        result = 31 * result + isDeleted.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(id=$id, clientId=$clientId, nickname='$nickname', profileImageUrl='$profileImageUrl', address='$address', addressLat=$addressLat, addressLog=$addressLog, alertAgreement=$alertAgreement, trackingAgreement=$trackingAgreement, isDeleted=$isDeleted)"
    }
}
