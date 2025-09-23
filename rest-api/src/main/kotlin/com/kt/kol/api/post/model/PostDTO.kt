package com.kt.kol.api.post.model

import java.time.LocalDateTime

data class PostDTO(
    val id: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val userId: Long? = null,
    val username: String? = null,
    val status: String? = null,
    val viewCount: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)