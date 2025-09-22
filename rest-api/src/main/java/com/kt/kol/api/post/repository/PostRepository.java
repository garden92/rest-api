package com.kt.kol.api.post.repository;

import com.kt.kol.api.post.model.Post;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PostRepository extends ReactiveCrudRepository<Post, Long> {

    Flux<Post> findByUserId(Long userId);

    Flux<Post> findByStatus(String status);

    @Query("SELECT * FROM posts WHERE title LIKE :keyword% OR content LIKE :keyword% ORDER BY created_at DESC")
    Flux<Post> searchByKeyword(String keyword);

    @Query("UPDATE posts SET view_count = view_count + 1 WHERE id = :id")
    Mono<Void> incrementViewCount(Long id);

    @Query("SELECT * FROM posts ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Post> findAllWithPagination(int limit, int offset);
}