package com.kt.kol.api.comment.model;

import java.time.LocalDateTime;

public record CommentDTO(
    Long id,
    String content,
    Long postId,
    Long userId,
    String username,
    Long parentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}