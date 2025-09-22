package com.kt.kol.api.comment.controller;

import com.kt.kol.api.comment.model.CommentDTO;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.PageDTO;
import com.kt.kol.api.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "댓글 관리 API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    @Operation(summary = "게시글의 댓글 조회", description = "특정 게시글의 댓글을 페이지네이션으로 조회합니다.")
    public Mono<ResponseStdVO<PageDTO<CommentDTO>>> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return commentService.findCommentsByPostIdPaged(postId, page, size)
                .map(ResponseStdVO::success);
    }

    @GetMapping("/post/{postId}/root")
    @Operation(summary = "게시글의 최상위 댓글 조회", description = "특정 게시글의 최상위 댓글만 조회합니다.")
    public Mono<ResponseStdVO<PageDTO<CommentDTO>>> getRootCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return commentService.findRootCommentsByPostIdPaged(postId, page, size)
                .map(ResponseStdVO::success);
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "댓글의 답글 조회", description = "특정 댓글의 답글들을 조회합니다.")
    public Mono<ResponseStdVO<PageDTO<CommentDTO>>> getRepliesByCommentId(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return commentService.findRepliesByCommentIdPaged(commentId, page, size)
                .map(ResponseStdVO::success);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다.")
    public Mono<ResponseStdVO<CommentDTO>> createComment(@RequestBody CommentDTO commentDTO) {
        return commentService.createComment(commentDTO)
                .map(ResponseStdVO::success);
    }

    @PutMapping("/{id}")
    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    public Mono<ResponseStdVO<CommentDTO>> updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        return commentService.updateComment(id, commentDTO)
                .map(ResponseStdVO::success);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public Mono<ResponseStdVO<Object>> deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id)
                .then(Mono.just(ResponseStdVO.success(null)));
    }

    @GetMapping("/post/{postId}/count")
    @Operation(summary = "댓글 개수 조회", description = "특정 게시글의 댓글 개수를 조회합니다.")
    public Mono<ResponseStdVO<Long>> countCommentsByPostId(@PathVariable Long postId) {
        return commentService.countCommentsByPostId(postId)
                .map(ResponseStdVO::success);
    }
}