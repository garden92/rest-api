package com.kt.kol.api.prechk.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "더미 데이터 전송 객체", description = "사전 체크 결과용 더미 데이터")
public record DummyDTO(
    @Schema(title = "더미 데이터", description = "더미 데이터 필드", example = "success")
    String dummy
) {
    public DummyDTO() {
        this("success");
    }
}
