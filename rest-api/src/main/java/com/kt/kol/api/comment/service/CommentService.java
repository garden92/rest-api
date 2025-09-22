package com.kt.kol.api.comment.service;

import com.kt.kol.api.comment.model.Comment;
import com.kt.kol.api.comment.model.CommentDTO;
import com.kt.kol.api.comment.repository.CommentRepository;
import com.kt.kol.api.post.repository.PostRepository;
import com.kt.kol.api.user.repository.UserRepository;
import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.model.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Mono<PageDTO<CommentDTO>> findCommentsByPostIdPaged(Long postId, int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = commentRepository.countByPostId(postId);
        Mono<List<CommentDTO>> dataMono = commentRepository.findByPostIdPage(postId, offset, size)
                .flatMap(this::enrichCommentWithUser)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<CommentDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
    }

    public Mono<PageDTO<CommentDTO>> findRootCommentsByPostIdPaged(Long postId, int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = commentRepository.countRootCommentsByPostId(postId);
        Mono<List<CommentDTO>> dataMono = commentRepository.findRootCommentsByPostIdPage(postId, offset, size)
                .flatMap(this::enrichCommentWithUser)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<CommentDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
    }

    public Mono<PageDTO<CommentDTO>> findRepliesByCommentIdPaged(Long commentId, int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = commentRepository.countByParentId(commentId);
        Mono<List<CommentDTO>> dataMono = commentRepository.findByParentIdPage(commentId, offset, size)
                .flatMap(this::enrichCommentWithUser)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<CommentDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
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
                .doOnSuccess(comment -> log.info("Comment created for post: {}", comment.postId()));
    }

    @Transactional
    public Mono<CommentDTO> updateComment(Long id, CommentDTO commentDTO) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")))
                .flatMap(existingComment -> {
                    existingComment.setContent(commentDTO.content());
                    existingComment.setUpdatedAt(LocalDateTime.now());
                    return commentRepository.save(existingComment);
                })
                .flatMap(this::enrichCommentWithUser)
                .doOnSuccess(comment -> log.info("Comment updated: {}", comment.id()));
    }

    @Transactional
    public Mono<Void> deleteComment(Long id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")))
                .flatMap(comment -> {
                    return commentRepository.countByParentId(id)
                            .flatMap(replyCount -> {
                                if (replyCount > 0) {
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
        return postRepository.existsById(commentDTO.postId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다."));
                    }
                    return userRepository.existsById(commentDTO.userId());
                })
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
                    }
                    if (commentDTO.parentId() != null) {
                        return commentRepository.existsById(commentDTO.parentId())
                                .flatMap(parentExists -> {
                                    if (!parentExists) {
                                        return Mono.error(
                                                new BusinessException("PARENT_COMMENT_NOT_FOUND", "부모 댓글을 찾을 수 없습니다."));
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
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getUserId(),
                username,
                comment.getParentId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    private Comment toEntity(CommentDTO dto) {
        return Comment.builder()
                .content(dto.content())
                .postId(dto.postId())
                .userId(dto.userId())
                .parentId(dto.parentId())
                .build();
    }
}