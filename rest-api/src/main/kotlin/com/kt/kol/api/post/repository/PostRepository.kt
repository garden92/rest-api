package com.kt.kol.api.post.repository

import com.kt.kol.api.post.model.Post
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : CoroutineCrudRepository<Post, Long> {

    @Query("UPDATE posts SET view_count = view_count + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: Long)

    @Query("SELECT * FROM posts ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findAllWithPagination(limit: Int, offset: Int): Flow<Post>

    @Query("SELECT * FROM posts WHERE user_id = :userId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    fun findByUserIdPage(userId: Long, offset: Long, size: Int): Flow<Post>

    @Query("SELECT COUNT(*) FROM posts WHERE user_id = :userId")
    suspend fun countByUserId(userId: Long): Long

    @Query("SELECT * FROM posts WHERE title LIKE :keyword% OR content LIKE :keyword% ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    fun searchByKeywordPage(keyword: String, offset: Long, size: Int): Flow<Post>

    @Query("SELECT COUNT(*) FROM posts WHERE title LIKE :keyword% OR content LIKE :keyword%")
    suspend fun countByKeywordSearch(keyword: String): Long
}