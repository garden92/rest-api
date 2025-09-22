package com.kt.kol.api.comment.repository;

import com.kt.kol.api.comment.model.Comment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    Flux<Comment> findByPostId(Long postId);

    Flux<Comment> findByUserId(Long userId);

    Flux<Comment> findByParentId(Long parentId);

    @Query("SELECT * FROM comments WHERE post_id = :postId AND parent_id IS NULL ORDER BY created_at DESC")
    Flux<Comment> findRootCommentsByPostId(Long postId);

    @Query("DELETE FROM comments WHERE post_id = :postId")
    Mono<Void> deleteByPostId(Long postId);

    Mono<Long> countByPostId(Long postId);
}