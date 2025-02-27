package com.deepromeet.atcha.user.domain

import jakarta.persistence.Embeddable

@Embeddable
class Agreement(
    var alert: Boolean = true,
    var tracking: Boolean = true
) {
    override fun toString(): String {
        return "Agreement(alertAgreement=$alert, trackingAgreement=$tracking)"
    }
}
