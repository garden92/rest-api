package com.kt.kol.api.user.repository

import com.kt.kol.api.user.model.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {

    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT :size OFFSET :offset")
    fun findPage(offset: Long, size: Int): Flow<User>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Long

    @Query("SELECT * FROM users WHERE first_name LIKE :name% OR last_name LIKE :name% ORDER BY id ASC LIMIT :size OFFSET :offset")
    fun searchByNamePage(name: String, offset: Long, size: Int): Flow<User>

    @Query("SELECT COUNT(*) FROM users WHERE first_name LIKE :name% OR last_name LIKE :name%")
    suspend fun countByNameSearch(name: String): Long

    suspend fun existsByUsername(username: String): Boolean

    suspend fun existsByEmail(email: String): Boolean
}