package com.kt.kol.api.user.repository;

import com.kt.kol.api.user.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByUsername(String username);

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT :size OFFSET :offset")
    Flux<User> findPage(long offset, int size);

    @Query("SELECT COUNT(*) FROM users")
    Mono<Long> countAll();

    @Query("SELECT * FROM users WHERE first_name LIKE :name% OR last_name LIKE :name% ORDER BY id ASC LIMIT :size OFFSET :offset")
    Flux<User> searchByNamePage(String name, long offset, int size);

    @Query("SELECT COUNT(*) FROM users WHERE first_name LIKE :name% OR last_name LIKE :name%")
    Mono<Long> countByNameSearch(String name);

    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByEmail(String email);
}