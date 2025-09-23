package com.kt.kol.api.comment.repository

import com.kt.kol.api.comment.model.Comment
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : CoroutineCrudRepository<Comment, Long> {

    @Query("DELETE FROM comments WHERE post_id = :postId")
    suspend fun deleteByPostId(postId: Long)

    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    fun findByPostIdPage(postId: Long, offset: Long, size: Int): Flow<Comment>

    @Query("SELECT * FROM comments WHERE post_id = :postId AND parent_id IS NULL ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    fun findRootCommentsByPostIdPage(postId: Long, offset: Long, size: Int): Flow<Comment>

    @Query("SELECT COUNT(*) FROM comments WHERE post_id = :postId AND parent_id IS NULL")
    suspend fun countRootCommentsByPostId(postId: Long): Long

    @Query("SELECT * FROM comments WHERE parent_id = :parentId ORDER BY created_at ASC LIMIT :size OFFSET :offset")
    fun findByParentIdPage(parentId: Long, offset: Long, size: Int): Flow<Comment>

    @Query("SELECT COUNT(*) FROM comments WHERE parent_id = :parentId")
    suspend fun countByParentId(parentId: Long): Long

    suspend fun countByPostId(postId: Long): Long
}