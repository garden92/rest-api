package com.kt.kol.api.post.controller

import com.kt.kol.api.post.model.PostDTO
import com.kt.kol.api.post.service.PostService
import com.kt.kol.common.model.PageDTO
import com.kt.kol.common.model.ResponseStdVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

private val log = LoggerFactory.getLogger(PostController::class.java)

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Post API", description = "게시글 관리 API")
class PostController(
    private val postService: PostService
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "게시글 목록 조회", description = "페이지네이션을 적용하여 게시글 목록을 조회합니다.")
    suspend fun getAllPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<PostDTO>> {
        return ResponseStdVO.success(postService.findAllPostsPaged(page, size))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "게시글 상세 조회", description = "ID로 특정 게시글을 조회합니다.")
    suspend fun getPostById(@PathVariable id: Long): ResponseStdVO<PostDTO> {
        return ResponseStdVO.success(postService.findPostById(id))
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자별 게시글 조회", description = "특정 사용자가 작성한 게시글을 조회합니다.")
    suspend fun getPostsByUserId(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<PostDTO>> {
        return ResponseStdVO.success(postService.findPostsByUserIdPaged(userId, page, size))
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    suspend fun createPost(@RequestBody postDTO: PostDTO): ResponseStdVO<PostDTO> {
        return ResponseStdVO.success(postService.createPost(postDTO))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    suspend fun updatePost(
        @PathVariable id: Long,
        @RequestBody postDTO: PostDTO
    ): ResponseStdVO<PostDTO> {
        return ResponseStdVO.success(postService.updatePost(id, postDTO))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    suspend fun deletePost(@PathVariable id: Long): ResponseStdVO<Any?> {
        postService.deletePost(id)
        return ResponseStdVO.success(null)
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "게시글 검색", description = "제목 또는 내용으로 게시글을 검색합니다.")
    suspend fun searchPosts(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<PostDTO>> {
        return ResponseStdVO.success(postService.searchPostsPaged(keyword, page, size))
    }
}