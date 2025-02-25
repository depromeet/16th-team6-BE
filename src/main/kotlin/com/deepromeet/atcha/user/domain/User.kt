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
    val kakaoId: Long,
    var nickname: String,
    var thumbnailImageUrl: String,
    var profileImageUrl: String,
    var alertAgreement: Boolean = true,
    var trackingAgreement: Boolean = true,
    var isDeleted: Boolean = false
) {
    override fun toString(): String {
        return "User(id=$id, kakaoId=$kakaoId, nickname='$nickname', thumbnailImageUrl='$thumbnailImageUrl', profileImageUrl='$profileImageUrl', alertAgreement=$alertAgreement, trackingAgreement=$trackingAgreement)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (kakaoId != other.kakaoId) return false
        if (nickname != other.nickname) return false
        if (thumbnailImageUrl != other.thumbnailImageUrl) return false
        if (profileImageUrl != other.profileImageUrl) return false
        if (alertAgreement != other.alertAgreement) return false
        if (trackingAgreement != other.trackingAgreement) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + kakaoId.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + thumbnailImageUrl.hashCode()
        result = 31 * result + profileImageUrl.hashCode()
        result = 31 * result + alertAgreement.hashCode()
        result = 31 * result + trackingAgreement.hashCode()
        return result
    }
}
