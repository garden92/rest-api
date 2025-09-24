package com.kt.kol.common.model

import kotlin.math.ceil

data class PageDTO<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> of(content: List<T>, page: Int, size: Int, totalElements: Long): PageDTO<T> {
            val totalPages = ceil(totalElements.toDouble() / size).toInt()
            val hasNext = page < totalPages - 1
            val hasPrevious = page > 0

            return PageDTO(content, page, size, totalElements, totalPages, hasNext, hasPrevious)
        }

        fun <T> empty(page: Int, size: Int): PageDTO<T> = PageDTO(
            emptyList(), page, size, 0L, 0, false, false
        )
    }
}