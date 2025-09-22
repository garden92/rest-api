package com.kt.kol.common.model;

import java.util.List;

public record PageDTO<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static <T> PageDTO<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new PageDTO<>(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }

    public static <T> PageDTO<T> empty(int page, int size) {
        return new PageDTO<>(List.of(), page, size, 0L, 0, false, false);
    }
}