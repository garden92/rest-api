package com.kt.kol.api.comment.controller;

import com.kt.kol.api.comment.model.CommentDTO;
import com.kt.kol.api.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "댓글 관리 API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    @Operation(summary = "게시글의 모든 댓글 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    public Flux<CommentDTO> getCommentsByPostId(@PathVariable Long postId) {
        return commentService.findCommentsByPostId(postId);
    }

    @GetMapping("/post/{postId}/root")
    @Operation(summary = "게시글의 최상위 댓글 조회", description = "특정 게시글의 최상위 댓글만 조회합니다.")
    public Flux<CommentDTO> getRootCommentsByPostId(@PathVariable Long postId) {
        return commentService.findRootCommentsByPostId(postId);
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "댓글의 답글 조회", description = "특정 댓글의 답글들을 조회합니다.")
    public Flux<CommentDTO> getRepliesByCommentId(@PathVariable Long commentId) {
        return commentService.findRepliesByCommentId(commentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다.")
    public Mono<CommentDTO> createComment(@RequestBody CommentDTO commentDTO) {
        return commentService.createComment(commentDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
    public Mono<CommentDTO> updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        return commentService.updateComment(id, commentDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public Mono<Void> deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }

    @GetMapping("/post/{postId}/count")
    @Operation(summary = "댓글 개수 조회", description = "특정 게시글의 댓글 개수를 조회합니다.")
    public Mono<Long> countCommentsByPostId(@PathVariable Long postId) {
        return commentService.countCommentsByPostId(postId);
    }
}