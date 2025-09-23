package com.kt.kol.api.comment.controller

import com.kt.kol.api.comment.model.CommentDTO
import com.kt.kol.api.comment.service.CommentService
import com.kt.kol.common.model.PageDTO
import com.kt.kol.common.model.ResponseStdVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

private val log = LoggerFactory.getLogger(CommentController::class.java)

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comment API", description = "댓글 관리 API")
class CommentController(
    private val commentService: CommentService
) {

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "게시글의 댓글 조회", description = "특정 게시글의 댓글을 페이지네이션으로 조회합니다.")
    suspend fun getCommentsByPostId(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<CommentDTO>> {
        return ResponseStdVO.success(commentService.findCommentsByPostIdPaged(postId, page, size))
    }

    @GetMapping("/post/{postId}/root")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "게시글의 최상위 댓글 조회", description = "특정 게시글의 최상위 댓글만 조회합니다.")
    suspend fun getRootCommentsByPostId(
        @PathVariable postId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<CommentDTO>> {
        return ResponseStdVO.success(commentService.findRootCommentsByPostIdPaged(postId, page, size))
    }

    @GetMapping("/{commentId}/replies")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글의 답글 조회", description = "특정 댓글의 답글들을 조회합니다.")
    suspend fun getRepliesByCommentId(
        @PathVariable commentId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseStdVO<PageDTO<CommentDTO>> {
        return ResponseStdVO.success(commentService.findRepliesByCommentIdPaged(commentId, page, size))
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다.")
    suspend fun createComment(@RequestBody commentDTO: CommentDTO): ResponseStdVO<CommentDTO> {
        return ResponseStdVO.success(commentService.createComment(commentDTO))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    suspend fun updateComment(
        @PathVariable id: Long,
        @RequestBody commentDTO: CommentDTO
    ): ResponseStdVO<CommentDTO> {
        return ResponseStdVO.success(commentService.updateComment(id, commentDTO))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    suspend fun deleteComment(@PathVariable id: Long): ResponseStdVO<Any?> {
        commentService.deleteComment(id)
        return ResponseStdVO.success(null)
    }

    @GetMapping("/post/{postId}/count")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "댓글 개수 조회", description = "특정 게시글의 댓글 개수를 조회합니다.")
    suspend fun countCommentsByPostId(@PathVariable postId: Long): ResponseStdVO<Long> {
        return ResponseStdVO.success(commentService.countCommentsByPostId(postId))
    }
}