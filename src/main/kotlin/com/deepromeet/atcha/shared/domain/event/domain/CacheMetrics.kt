package com.deepromeet.atcha.shared.domain.event.domain

data class CacheMetrics(
    val hit: Long,
    val miss: Long
) {
    val total: Long get() = hit + miss
    val hitRate: Double get() = if (total == 0L) 0.0 else hit.toDouble() / total
}
