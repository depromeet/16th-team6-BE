package com.deepromeet.atcha.transit.infrastructure.utils

import com.deepromeet.atcha.transit.domain.TransitNameComparer
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.springframework.stereotype.Component

@Component
class SimilarTransitNameComparer() : TransitNameComparer {
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.92
    }

    private val similarity = JaroWinklerSimilarity()

    override fun isSame(
        name1: String,
        name2: String
    ): Boolean {
        if (name1 == "남문시장.청춘삘딩") {
            println("Comparing: $name1 and $name2")
        }
        val score = similarity.apply(name1, name2)
        return score >= SIMILARITY_THRESHOLD
    }
}
