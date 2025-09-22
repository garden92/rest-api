package com.kt.kol.api.user.model;

import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}