package com.deepromeet.atcha.transit.application

interface TransitNameComparer {
    fun isSame(
        name1: String?,
        name2: String?
    ): Boolean
}
