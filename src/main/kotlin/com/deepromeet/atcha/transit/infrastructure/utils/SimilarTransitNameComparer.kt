package com.deepromeet.atcha.transit.infrastructure.utils

import com.deepromeet.atcha.transit.domain.TransitNameComparer
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.springframework.stereotype.Component

@Component
class SimilarTransitNameComparer() : TransitNameComparer {
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.85
    }

    private val similarity = JaroWinklerSimilarity()

    override fun isSame(
        name1: String,
        name2: String
    ): Boolean {
        val score = similarity.apply(normalize(name1), normalize(name2))
        return score >= SIMILARITY_THRESHOLD
    }

    private fun normalize(name: String): String =
        name.replace("(지하)", "")
            .replace("(중)", "")
            .trim()
}
