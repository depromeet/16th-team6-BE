package com.deepromeet.atcha.transit.domain

interface TransitNameComparer {
    fun isSame(
        name1: String,
        name2: String
    ): Boolean
}
