package com.kt.kol.api.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.kol.api.user.model.UserDTO;
import com.kt.kol.api.user.service.UserService;
import com.kt.kol.common.model.ResponseStdVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "모든 사용자 조회", description = "시스템에 등록된 모든 사용자를 조회합니다.")
    public Mono<ResponseEntity<ResponseStdVO<Flux<UserDTO>>>> getAllUsers() {
        return Mono.just(ResponseEntity.ok(ResponseStdVO.success(userService.findAllUsers())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자를 조회합니다.")
    public Mono<ResponseEntity<ResponseStdVO<UserDTO>>> getUserById(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(user -> ResponseEntity.ok(ResponseStdVO.success(user)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "사용자명으로 조회", description = "사용자명으로 사용자를 조회합니다.")
    public Mono<ResponseEntity<ResponseStdVO<UserDTO>>> getUserByUsername(@PathVariable String username) {
        return userService.findUserByUsername(username)
                .map(user -> ResponseEntity.ok(ResponseStdVO.success(user)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    public Mono<ResponseEntity<ResponseStdVO<UserDTO>>> createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ResponseStdVO.success(user)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "사용자 수정", description = "기존 사용자 정보를 수정합니다.")
    public Mono<ResponseEntity<ResponseStdVO<UserDTO>>> updateUser(@PathVariable Long id,
            @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO)
                .map(user -> ResponseEntity.ok(ResponseStdVO.success(user)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    public Mono<ResponseEntity<ResponseStdVO<Void>>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ResponseEntity.ok(ResponseStdVO.success(null))))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "사용자 검색", description = "이름으로 사용자를 검색합니다.")
    public Mono<ResponseEntity<ResponseStdVO<Flux<UserDTO>>>> searchUsers(@RequestParam String query) {
        return Mono.just(ResponseEntity.ok(ResponseStdVO.success(userService.searchUsers(query))));
    }
}