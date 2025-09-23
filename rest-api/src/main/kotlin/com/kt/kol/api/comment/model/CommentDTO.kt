package com.kt.kol.api.comment.model

import java.time.LocalDateTime

data class CommentDTO(
    val id: Long? = null,
    val content: String? = null,
    val postId: Long? = null,
    val userId: Long? = null,
    val username: String? = null,
    val parentId: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)