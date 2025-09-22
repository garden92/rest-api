package com.kt.kol.api.post.service;

import com.kt.kol.api.post.model.Post;
import com.kt.kol.api.post.model.PostDTO;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Mono<PageDTO<PostDTO>> findAllPostsPaged(int page, int size) {
        int offset = page * size;

        Mono<Long> countMono = postRepository.count();
        Mono<List<PostDTO>> dataMono = postRepository.findAllWithPagination(size, offset)
                .flatMap(this::enrichPostWithUser)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<PostDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
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
        return userRepository.existsById(postDTO.userId())
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
                .doOnSuccess(post -> log.info("Post created: {}", post.title()));
    }

    @Transactional
    public Mono<PostDTO> updatePost(Long id, PostDTO postDTO) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")))
                .flatMap(existingPost -> {
                    existingPost.setTitle(postDTO.title());
                    existingPost.setContent(postDTO.content());
                    existingPost.setStatus(postDTO.status());
                    existingPost.setUpdatedAt(LocalDateTime.now());
                    return postRepository.save(existingPost);
                })
                .flatMap(this::enrichPostWithUser)
                .doOnSuccess(post -> log.info("Post updated: {}", post.title()));
    }

    @Transactional
    public Mono<Void> deletePost(Long id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")))
                .flatMap(post -> postRepository.delete(post))
                .doOnSuccess(v -> log.info("Post deleted: {}", id));
    }

    public Mono<PageDTO<PostDTO>> findPostsByUserIdPaged(Long userId, int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = postRepository.countByUserId(userId);
        Mono<List<PostDTO>> dataMono = postRepository.findByUserIdPage(userId, offset, size)
                .flatMap(this::enrichPostWithUser)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<PostDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
    }

    public Mono<PageDTO<PostDTO>> searchPostsPaged(String keyword, int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = postRepository.countByKeywordSearch(keyword);
        Mono<List<PostDTO>> dataMono = postRepository.searchByKeywordPage(keyword, offset, size)
                .flatMap(this::enrichPostWithUser)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<PostDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
    }

    private Mono<PostDTO> enrichPostWithUser(Post post) {
        return userRepository.findById(post.getUserId())
                .map(user -> toDTO(post, user.getUsername()))
                .defaultIfEmpty(toDTO(post, "Unknown"));
    }

    private PostDTO toDTO(Post post, String username) {
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUserId(),
                username,
                post.getStatus(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private Post toEntity(PostDTO dto) {
        return Post.builder()
                .title(dto.title())
                .content(dto.content())
                .userId(dto.userId())
                .status(dto.status())
                .build();
    }
}