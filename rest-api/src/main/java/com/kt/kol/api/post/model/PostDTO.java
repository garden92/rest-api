package com.kt.kol.api.post.model;

import java.time.LocalDateTime;

public record PostDTO(
    Long id,
    String title,
    String content,
    Long userId,
    String username,
    String status,
    Integer viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}