package com.kt.kol.api.user.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    val id: Long? = null,
    var username: String? = null,
    var email: String? = null,
    var password: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var active: Boolean? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
)