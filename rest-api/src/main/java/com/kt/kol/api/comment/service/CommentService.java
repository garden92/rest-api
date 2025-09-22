package com.kt.kol.api.comment.service;

import com.kt.kol.api.comment.model.Comment;
import com.kt.kol.api.comment.model.CommentDTO;
import com.kt.kol.api.comment.repository.CommentRepository;
import com.kt.kol.api.post.repository.PostRepository;
import com.kt.kol.api.user.repository.UserRepository;
import com.kt.kol.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Flux<CommentDTO> findCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId)
                .flatMap(this::enrichCommentWithUser);
    }

    public Flux<CommentDTO> findRootCommentsByPostId(Long postId) {
        return commentRepository.findRootCommentsByPostId(postId)
                .flatMap(this::enrichCommentWithUser);
    }

    public Flux<CommentDTO> findRepliesByCommentId(Long commentId) {
        return commentRepository.findByParentId(commentId)
                .flatMap(this::enrichCommentWithUser);
    }

    @Transactional
    public Mono<CommentDTO> createComment(CommentDTO commentDTO) {
        return validateCommentCreation(commentDTO)
                .then(Mono.defer(() -> {
                    Comment comment = toEntity(commentDTO);
                    comment.setCreatedAt(LocalDateTime.now());
                    comment.setUpdatedAt(LocalDateTime.now());
                    return commentRepository.save(comment);
                }))
                .flatMap(this::enrichCommentWithUser)
                .doOnSuccess(comment -> log.info("Comment created for post: {}", comment.getPostId()));
    }

    @Transactional
    public Mono<CommentDTO> updateComment(Long id, CommentDTO commentDTO) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")))
                .flatMap(existingComment -> {
                    existingComment.setContent(commentDTO.getContent());
                    existingComment.setUpdatedAt(LocalDateTime.now());
                    return commentRepository.save(existingComment);
                })
                .flatMap(this::enrichCommentWithUser)
                .doOnSuccess(comment -> log.info("Comment updated: {}", comment.getId()));
    }

    @Transactional
    public Mono<Void> deleteComment(Long id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")))
                .flatMap(comment -> {
                    return commentRepository.findByParentId(id)
                            .collectList()
                            .flatMap(replies -> {
                                if (!replies.isEmpty()) {
                                    return Mono.error(new BusinessException("HAS_REPLIES", "답글이 있는 댓글은 삭제할 수 없습니다."));
                                }
                                return commentRepository.delete(comment);
                            });
                })
                .doOnSuccess(v -> log.info("Comment deleted: {}", id));
    }

    public Mono<Long> countCommentsByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    private Mono<Void> validateCommentCreation(CommentDTO commentDTO) {
        return postRepository.existsById(commentDTO.getPostId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
                    }
                    return userRepository.existsById(commentDTO.getUserId());
                })
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
                    }
                    if (commentDTO.getParentId() != null) {
                        return commentRepository.existsById(commentDTO.getParentId())
                                .flatMap(parentExists -> {
                                    if (!parentExists) {
                                        return Mono.error(new BusinessException("PARENT_COMMENT_NOT_FOUND", "부모 댓글을 찾을 수 없습니다."));
                                    }
                                    return Mono.empty();
                                });
                    }
                    return Mono.empty();
                });
    }

    private Mono<CommentDTO> enrichCommentWithUser(Comment comment) {
        return userRepository.findById(comment.getUserId())
                .map(user -> toDTO(comment, user.getUsername()))
                .defaultIfEmpty(toDTO(comment, "Unknown"));
    }

    private CommentDTO toDTO(Comment comment, String username) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(username)
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private Comment toEntity(CommentDTO dto) {
        return Comment.builder()
                .content(dto.getContent())
                .postId(dto.getPostId())
                .userId(dto.getUserId())
                .parentId(dto.getParentId())
                .build();
    }
}