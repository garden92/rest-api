package com.kt.kol.api.post.controller;

import com.kt.kol.api.post.model.PostDTO;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.PageDTO;
import com.kt.kol.api.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post API", description = "게시글 관리 API")
public class PostController {

	private final PostService postService;

	@GetMapping
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "게시글 목록 조회", description = "페이지네이션을 적용하여 게시글 목록을 조회합니다.")
	public Mono<ResponseStdVO<PageDTO<PostDTO>>> getAllPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.findAllPostsPaged(page, size)
				.map(ResponseStdVO::success);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "게시글 상세 조회", description = "ID로 특정 게시글을 조회합니다.")
	public Mono<ResponseStdVO<PostDTO>> getPostById(@PathVariable Long id) {
		return postService.findPostById(id)
				.map(ResponseStdVO::success);
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "사용자별 게시글 조회", description = "특정 사용자가 작성한 게시글을 조회합니다.")
	public Mono<ResponseStdVO<PageDTO<PostDTO>>> getPostsByUserId(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.findPostsByUserIdPaged(userId, page, size)
				.map(ResponseStdVO::success);
	}

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
	public Mono<ResponseStdVO<PostDTO>> createPost(@RequestBody PostDTO postDTO) {
		return postService.createPost(postDTO)
				.map(ResponseStdVO::success);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
	public Mono<ResponseStdVO<PostDTO>> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
		return postService.updatePost(id, postDTO)
				.map(ResponseStdVO::success);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('USER')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
	public Mono<ResponseStdVO<Object>> deletePost(@PathVariable Long id) {
		return postService.deletePost(id)
				.then(Mono.just(ResponseStdVO.success(null)));
	}

	@GetMapping("/search")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "게시글 검색", description = "제목 또는 내용으로 게시글을 검색합니다.")
	public Mono<ResponseStdVO<PageDTO<PostDTO>>> searchPosts(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return postService.searchPostsPaged(keyword, page, size)
				.map(ResponseStdVO::success);
	}
}