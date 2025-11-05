package com.deepromeet.atcha.transit.infrastructure.utils

import com.deepromeet.atcha.transit.application.TransitNameComparer
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.springframework.stereotype.Component

@Component
class SimilarTransitNameComparer() : TransitNameComparer {
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.9
    }

    private val similarity = JaroWinklerSimilarity()

    override fun isSame(
        name1: String?,
        name2: String?
    ): Boolean {
        if (name1.isNullOrBlank() || name2.isNullOrBlank()) {
            return false
        }

        val normalized1 = normalize(name1)
        val normalized2 = normalize(name2)

        if (normalized1 == normalized2) return true

        if (hasPartialMatch(normalized1, normalized2)) return true

        val score = similarity.apply(normalized1, normalized2)
        return score >= SIMILARITY_THRESHOLD
    }

    private fun hasPartialMatch(
        name1: String,
        name2: String
    ): Boolean {
        val parts1 = name1.split(".").map { it.trim() }.filter { it.isNotEmpty() }
        val parts2 = name2.split(".").map { it.trim() }.filter { it.isNotEmpty() }

        if (parts1.size == 1 && parts2.size > 1) {
            return parts2.any { part ->
                normalize(part) == normalize(parts1[0]) ||
                    normalize(part).contains(normalize(parts1[0])) ||
                    normalize(parts1[0]).contains(normalize(part))
            }
        }

        if (parts2.size == 1 && parts1.size > 1) {
            return parts1.any { part ->
                normalize(part) == normalize(parts2[0]) ||
                    normalize(part).contains(normalize(parts2[0])) ||
                    normalize(parts2[0]).contains(normalize(part))
            }
        }

        if (parts1.size > 1 && parts2.size > 1) {
            return parts1.any { part1 ->
                parts2.any { part2 ->
                    normalize(part1) == normalize(part2) ||
                        normalize(part1).contains(normalize(part2)) ||
                        normalize(part2).contains(normalize(part1))
                }
            }
        }

        return false
    }

    private fun normalize(name: String): String =
        name.replace("(지하)", "")
            .replace("(중)", "")
            .trim()
}
