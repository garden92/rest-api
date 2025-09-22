package com.kt.kol.api.user.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.kol.api.user.model.User;
import com.kt.kol.api.user.model.UserDTO;
import com.kt.kol.api.user.repository.UserRepository;
import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.model.PageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<PageDTO<UserDTO>> findAllUsersPaged(int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = userRepository.countAll();
        Mono<List<UserDTO>> dataMono = userRepository.findPage(offset, size)
                .map(this::toDTO)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<UserDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
    }

    public Mono<UserDTO> findUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .switchIfEmpty(Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")));
    }

    public Mono<UserDTO> findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDTO)
                .switchIfEmpty(Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")));
    }

    @Transactional
    public Mono<UserDTO> createUser(UserDTO userDTO) {
        return userRepository.existsByUsername(userDTO.username())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException("USER_EXISTS", "이미 존재하는 사용자명입니다."));
                    }
                    return userRepository.existsByEmail(userDTO.email());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException("EMAIL_EXISTS", "이미 존재하는 이메일입니다."));
                    }
                    User user = toEntity(userDTO);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    user.setActive(true);
                    return userRepository.save(user);
                })
                .map(this::toDTO)
                .doOnSuccess(user -> log.info("User created: {}", user.username()));
    }

    @Transactional
    public Mono<UserDTO> updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")))
                .flatMap(existingUser -> {
                    existingUser.setFirstName(userDTO.firstName());
                    existingUser.setLastName(userDTO.lastName());
                    existingUser.setEmail(userDTO.email());
                    existingUser.setActive(userDTO.active());
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(existingUser);
                })
                .map(this::toDTO)
                .doOnSuccess(user -> log.info("User updated: {}", user.username()));
    }

    @Transactional
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")))
                .flatMap(user -> userRepository.delete(user))
                .doOnSuccess(v -> log.info("User deleted: {}", id));
    }

    public Mono<PageDTO<UserDTO>> searchUsersPaged(String query, int page, int size) {
        long offset = (long) page * size;

        Mono<Long> countMono = userRepository.countByNameSearch(query);
        Mono<List<UserDTO>> dataMono = userRepository.searchByNamePage(query, offset, size)
                .map(this::toDTO)
                .collectList();

        return Mono.zip(countMono, dataMono)
                .map(tuple -> {
                    Long totalElements = tuple.getT1();
                    List<UserDTO> content = tuple.getT2();

                    if (totalElements == 0) {
                        return PageDTO.empty(page, size);
                    }
                    return PageDTO.of(content, page, size, totalElements);
                });
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private User toEntity(UserDTO dto) {
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .active(dto.active())
                .build();
    }
}