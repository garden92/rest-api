package com.kt.kol.app.security

data class JwtUserPrincipal(
    val userId: String,
    val username: String,
    val roles: List<String>
) {
    fun hasRole(role: String): Boolean {
        return roles.contains(role)
    }

    fun hasAnyRole(vararg roles: String): Boolean {
        return roles.any { this.roles.contains(it) }
    }
}