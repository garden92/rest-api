package com.kt.kol.api.user.model

import java.time.LocalDateTime

data class UserDTO(
    val id: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val active: Boolean? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)