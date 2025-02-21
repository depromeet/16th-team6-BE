package com.deepromeet.seulseul.user.domain

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val kakaoId: Long,
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
    val alertAgreement: Boolean = true,
    val trackingAgreement: Boolean = true,
)
