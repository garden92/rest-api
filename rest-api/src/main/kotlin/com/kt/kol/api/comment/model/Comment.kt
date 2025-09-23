package com.kt.kol.api.comment.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("comments")
data class Comment(
    @Id
    val id: Long? = null,
    var content: String? = null,
    var postId: Long? = null,
    var userId: Long? = null,
    var parentId: Long? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
)