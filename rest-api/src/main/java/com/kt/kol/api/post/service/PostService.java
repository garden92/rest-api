package com.kt.kol.api.post.service;

import com.kt.kol.api.post.model.Post;
import com.kt.kol.api.post.model.PostDTO;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Flux<PostDTO> findAllPosts(int page, int size) {
        int offset = page * size;
        return postRepository.findAllWithPagination(size, offset)
                .flatMap(this::enrichPostWithUser);
    }

    public Mono<PostDTO> findPostById(Long id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")))
                .flatMap(post -> {
                    postRepository.incrementViewCount(id).subscribe();
                    return enrichPostWithUser(post);
                });
    }

    @Transactional
    public Mono<PostDTO> createPost(PostDTO postDTO) {
        return userRepository.existsById(postDTO.getUserId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
                    }
                    Post post = toEntity(postDTO);
                    post.setCreatedAt(LocalDateTime.now());
                    post.setUpdatedAt(LocalDateTime.now());
                    post.setStatus("PUBLISHED");
                    post.setViewCount(0);
                    return postRepository.save(post);
                })
                .flatMap(this::enrichPostWithUser)
                .doOnSuccess(post -> log.info("Post created: {}", post.getTitle()));
    }

    @Transactional
    public Mono<PostDTO> updatePost(Long id, PostDTO postDTO) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")))
                .flatMap(existingPost -> {
                    existingPost.setTitle(postDTO.getTitle());
                    existingPost.setContent(postDTO.getContent());
                    existingPost.setStatus(postDTO.getStatus());
                    existingPost.setUpdatedAt(LocalDateTime.now());
                    return postRepository.save(existingPost);
                })
                .flatMap(this::enrichPostWithUser)
                .doOnSuccess(post -> log.info("Post updated: {}", post.getTitle()));
    }

    @Transactional
    public Mono<Void> deletePost(Long id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")))
                .flatMap(post -> postRepository.delete(post))
                .doOnSuccess(v -> log.info("Post deleted: {}", id));
    }

    public Flux<PostDTO> findPostsByUserId(Long userId) {
        return postRepository.findByUserId(userId)
                .flatMap(this::enrichPostWithUser);
    }

    public Flux<PostDTO> searchPosts(String keyword) {
        return postRepository.searchByKeyword(keyword)
                .flatMap(this::enrichPostWithUser);
    }

    private Mono<PostDTO> enrichPostWithUser(Post post) {
        return userRepository.findById(post.getUserId())
                .map(user -> toDTO(post, user.getUsername()))
                .defaultIfEmpty(toDTO(post, "Unknown"));
    }

    private PostDTO toDTO(Post post, String username) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUserId())
                .username(username)
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private Post toEntity(PostDTO dto) {
        return Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .userId(dto.getUserId())
                .status(dto.getStatus())
                .build();
    }
}