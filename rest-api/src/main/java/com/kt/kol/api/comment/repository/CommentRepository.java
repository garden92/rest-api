package com.kt.kol.api.comment.repository;

import com.kt.kol.api.comment.model.Comment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    @Query("DELETE FROM comments WHERE post_id = :postId")
    Mono<Void> deleteByPostId(Long postId);

    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<Comment> findByPostIdPage(Long postId, long offset, int size);

    @Query("SELECT * FROM comments WHERE post_id = :postId AND parent_id IS NULL ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<Comment> findRootCommentsByPostIdPage(Long postId, long offset, int size);

    @Query("SELECT COUNT(*) FROM comments WHERE post_id = :postId AND parent_id IS NULL")
    Mono<Long> countRootCommentsByPostId(Long postId);

    @Query("SELECT * FROM comments WHERE parent_id = :parentId ORDER BY created_at ASC LIMIT :size OFFSET :offset")
    Flux<Comment> findByParentIdPage(Long parentId, long offset, int size);

    @Query("SELECT COUNT(*) FROM comments WHERE parent_id = :parentId")
    Mono<Long> countByParentId(Long parentId);

    Mono<Long> countByPostId(Long postId);
}