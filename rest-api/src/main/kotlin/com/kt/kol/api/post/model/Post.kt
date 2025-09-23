package com.kt.kol.api.post.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("posts")
data class Post(
    @Id
    val id: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var userId: Long? = null,
    var status: String? = null,
    var viewCount: Int? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
)