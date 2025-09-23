package com.kt.kol.api.comment.service

import com.kt.kol.api.comment.model.Comment
import com.kt.kol.api.comment.model.CommentDTO
import com.kt.kol.api.comment.repository.CommentRepository
import com.kt.kol.api.post.repository.PostRepository
import com.kt.kol.api.user.repository.UserRepository
import com.kt.kol.common.exception.BusinessException
import com.kt.kol.common.model.PageDTO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger(CommentService::class.java)

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    suspend fun findCommentsByPostIdPaged(postId: Long, page: Int, size: Int): PageDTO<CommentDTO> {
        val offset = page.toLong() * size

        val totalElements = commentRepository.countByPostId(postId)
        val content = commentRepository.findByPostIdPage(postId, offset, size)
            .map { enrichCommentWithUser(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    suspend fun findRootCommentsByPostIdPaged(postId: Long, page: Int, size: Int): PageDTO<CommentDTO> {
        val offset = page.toLong() * size

        val totalElements = commentRepository.countRootCommentsByPostId(postId)
        val content = commentRepository.findRootCommentsByPostIdPage(postId, offset, size)
            .map { enrichCommentWithUser(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    suspend fun findRepliesByCommentIdPaged(commentId: Long, page: Int, size: Int): PageDTO<CommentDTO> {
        val offset = page.toLong() * size

        val totalElements = commentRepository.countByParentId(commentId)
        val content = commentRepository.findByParentIdPage(commentId, offset, size)
            .map { enrichCommentWithUser(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    @Transactional
    suspend fun createComment(commentDTO: CommentDTO): CommentDTO {
        validateCommentCreation(commentDTO)

        val comment = toEntity(commentDTO).apply {
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val savedComment = commentRepository.save(comment)
        val result = enrichCommentWithUser(savedComment)
        log.info("Comment created for post: ${result.postId}")
        return result
    }

    @Transactional
    suspend fun updateComment(id: Long, commentDTO: CommentDTO): CommentDTO {
        val existingComment = commentRepository.findById(id)
            ?: throw BusinessException("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")

        existingComment.apply {
            content = commentDTO.content
            updatedAt = LocalDateTime.now()
        }

        val savedComment = commentRepository.save(existingComment)
        val result = enrichCommentWithUser(savedComment)
        log.info("Comment updated: ${result.id}")
        return result
    }

    @Transactional
    suspend fun deleteComment(id: Long) {
        val comment = commentRepository.findById(id)
            ?: throw BusinessException("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")

        val replyCount = commentRepository.countByParentId(id)
        if (replyCount > 0) {
            throw BusinessException("HAS_REPLIES", "답글이 있는 댓글은 삭제할 수 없습니다.")
        }

        commentRepository.delete(comment)
        log.info("Comment deleted: $id")
    }

    suspend fun countCommentsByPostId(postId: Long): Long {
        return commentRepository.countByPostId(postId)
    }

    private suspend fun validateCommentCreation(commentDTO: CommentDTO) {
        val postExists = postRepository.existsById(commentDTO.postId!!)
        if (!postExists) {
            throw BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")
        }

        val userExists = userRepository.existsById(commentDTO.userId!!)
        if (!userExists) {
            throw BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }

        commentDTO.parentId?.let { parentId ->
            val parentExists = commentRepository.existsById(parentId)
            if (!parentExists) {
                throw BusinessException("PARENT_COMMENT_NOT_FOUND", "부모 댓글을 찾을 수 없습니다.")
            }
        }
    }

    private suspend fun enrichCommentWithUser(comment: Comment): CommentDTO {
        val username = try {
            userRepository.findById(comment.userId!!)?.username ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
        return toDTO(comment, username)
    }

    private fun toDTO(comment: Comment, username: String): CommentDTO {
        return CommentDTO(
            id = comment.id,
            content = comment.content,
            postId = comment.postId,
            userId = comment.userId,
            username = username,
            parentId = comment.parentId,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt
        )
    }

    private fun toEntity(dto: CommentDTO): Comment {
        return Comment(
            content = dto.content,
            postId = dto.postId,
            userId = dto.userId,
            parentId = dto.parentId
        )
    }
}