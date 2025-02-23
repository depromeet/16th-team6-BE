package com.deepromeet.atcha.common.web

import org.springframework.data.domain.Slice

data class SliceResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
) {
    companion object {
        fun <T> from(slice: Slice<T>): SliceResponse<T> {
            return SliceResponse(
                content = slice.content,
                page = slice.number,
                size = slice.size,
                hasNext = slice.hasNext()
            )
        }
    }
}
