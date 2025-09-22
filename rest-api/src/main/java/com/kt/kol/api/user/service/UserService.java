package com.kt.kol.api.user.service;

import com.kt.kol.api.user.model.User;
import com.kt.kol.api.user.model.UserDTO;
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
public class UserService {

    private final UserRepository userRepository;

    public Flux<UserDTO> findAllUsers() {
        return userRepository.findAll()
                .map(this::toDTO);
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
        return userRepository.existsByUsername(userDTO.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException("USER_EXISTS", "이미 존재하는 사용자명입니다."));
                    }
                    return userRepository.existsByEmail(userDTO.getEmail());
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
                .doOnSuccess(user -> log.info("User created: {}", user.getUsername()));
    }

    @Transactional
    public Mono<UserDTO> updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")))
                .flatMap(existingUser -> {
                    existingUser.setFirstName(userDTO.getFirstName());
                    existingUser.setLastName(userDTO.getLastName());
                    existingUser.setEmail(userDTO.getEmail());
                    existingUser.setActive(userDTO.getActive());
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(existingUser);
                })
                .map(this::toDTO)
                .doOnSuccess(user -> log.info("User updated: {}", user.getUsername()));
    }

    @Transactional
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")))
                .flatMap(user -> userRepository.delete(user))
                .doOnSuccess(v -> log.info("User deleted: {}", id));
    }

    public Flux<UserDTO> searchUsers(String query) {
        return userRepository.searchByName(query)
                .map(this::toDTO);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User toEntity(UserDTO dto) {
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .active(dto.getActive())
                .build();
    }
}