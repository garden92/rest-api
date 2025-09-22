package com.kt.kol.api.comment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private String username;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}