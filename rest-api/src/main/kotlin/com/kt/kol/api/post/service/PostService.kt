package com.kt.kol.api.post.service

import com.kt.kol.api.post.model.Post
import com.kt.kol.api.post.model.PostDTO
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

private val log = LoggerFactory.getLogger(PostService::class.java)

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    suspend fun findAllPostsPaged(page: Int, size: Int): PageDTO<PostDTO> {
        val offset = page * size

        val totalElements = postRepository.count()
        val content = postRepository.findAllWithPagination(size, offset)
            .map { enrichPostWithUser(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    suspend fun findPostById(id: Long): PostDTO {
        val post = postRepository.findById(id)
            ?: throw BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")

        postRepository.incrementViewCount(id)
        return enrichPostWithUser(post)
    }

    @Transactional
    suspend fun createPost(postDTO: PostDTO): PostDTO {
        val userExists = userRepository.existsById(postDTO.userId!!)
        if (!userExists) {
            throw BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }

        val post = toEntity(postDTO).apply {
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
            status = "PUBLISHED"
            viewCount = 0
        }

        val savedPost = postRepository.save(post)
        val result = enrichPostWithUser(savedPost)
        log.info("Post created: ${result.title}")
        return result
    }

    @Transactional
    suspend fun updatePost(id: Long, postDTO: PostDTO): PostDTO {
        val existingPost = postRepository.findById(id)
            ?: throw BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")

        existingPost.apply {
            title = postDTO.title
            content = postDTO.content
            status = postDTO.status
            updatedAt = LocalDateTime.now()
        }

        val savedPost = postRepository.save(existingPost)
        val result = enrichPostWithUser(savedPost)
        log.info("Post updated: ${result.title}")
        return result
    }

    @Transactional
    suspend fun deletePost(id: Long) {
        val post = postRepository.findById(id)
            ?: throw BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")

        postRepository.delete(post)
        log.info("Post deleted: $id")
    }

    suspend fun findPostsByUserIdPaged(userId: Long, page: Int, size: Int): PageDTO<PostDTO> {
        val offset = page.toLong() * size

        val totalElements = postRepository.countByUserId(userId)
        val content = postRepository.findByUserIdPage(userId, offset, size)
            .map { enrichPostWithUser(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    suspend fun searchPostsPaged(keyword: String, page: Int, size: Int): PageDTO<PostDTO> {
        val offset = page.toLong() * size

        val totalElements = postRepository.countByKeywordSearch(keyword)
        val content = postRepository.searchByKeywordPage(keyword, offset, size)
            .map { enrichPostWithUser(it) }
            .toList()

        return if (totalElements == 0L) {
            PageDTO.empty(page, size)
        } else {
            PageDTO.of(content, page, size, totalElements)
        }
    }

    private suspend fun enrichPostWithUser(post: Post): PostDTO {
        val username = try {
            userRepository.findById(post.userId!!)?.username ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
        return toDTO(post, username)
    }

    private fun toDTO(post: Post, username: String): PostDTO {
        return PostDTO(
            id = post.id,
            title = post.title,
            content = post.content,
            userId = post.userId,
            username = username,
            status = post.status,
            viewCount = post.viewCount,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt
        )
    }

    private fun toEntity(dto: PostDTO): Post {
        return Post(
            title = dto.title,
            content = dto.content,
            userId = dto.userId,
            status = dto.status
        )
    }
}