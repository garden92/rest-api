package com.kt.kol.api.encryption.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.kol.api.encryption.model.EncryptionPolicyDTO;
import com.kt.kol.api.encryption.model.EncryptionPolicyResponseDTO;
import com.kt.kol.api.encryption.model.EncryptionRequestDTO;
import com.kt.kol.api.encryption.service.EncryptionPolicyService;
import com.kt.kol.common.model.ResponseStdVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Tag(name = "ENCRYPTION", description = "암호화 정책 API")
@RestController
@RequestMapping("/restGw/encryption")
@Slf4j
public class EncryptionPolicyController {

    private final EncryptionPolicyService encryptionPolicyService;

    @Operation(summary = "암호화 정책 및 키 정보 조회", description = """
            특정 채널이 호출한 API Path의 보안필드와 보안 키 값을 GW쪽에 전달합니다.
            - 채널 ID, API 경로, HTTP 메서드를 기준으로 암호화 정책 조회
            - 해당 채널의 활성화된 암호화 키 정보 반환
            - 암호화/복호화가 필요한 필드 목록과 방향(IN/OUT/BOTH) 제공
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "암호화 정책 조회 성공", 
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EncryptionPolicyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "암호화 정책 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/policy")
    public Mono<ResponseStdVO<EncryptionPolicyResponseDTO>> getEncryptionPolicy(
            @RequestBody @Parameter(description = "암호화 정책 요청 정보", required = true) 
            EncryptionRequestDTO requestDTO) {
        
        log.info("암호화 정책 조회 API 호출. clientId=[{}], apiPath=[{}], method=[{}]", 
                requestDTO.clientId(), requestDTO.apiPath(), requestDTO.httpMethod());
        
        return encryptionPolicyService.getEncryptionPolicy(requestDTO);
    }

    @Operation(summary = "채널별 전체 암호화 정책 조회", description = """
            특정 채널의 모든 암호화 정책을 조회합니다. (관리용)
            - 해당 채널에 등록된 모든 API 경로의 암호화 정책 반환
            - 시스템 관리 및 정책 검토 목적으로 사용
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "암호화 정책 목록 조회 성공", 
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/policy/{clientId}")
    public Mono<ResponseStdVO<java.util.List<EncryptionPolicyDTO>>> getAllEncryptionPoliciesByClient(
            @PathVariable @Parameter(description = "클라이언트 ID", required = true, example = "MOBILE_APP") 
            String clientId) {
        
        log.info("채널별 전체 암호화 정책 조회 API 호출. clientId=[{}]", clientId);
        
        return encryptionPolicyService.getAllEncryptionPoliciesByClient(clientId);
    }

    @Operation(summary = "암호화 정책 상태 확인", description = """
            특정 채널과 API 경로에 대한 암호화 정책 존재 여부를 간단히 확인합니다.
            - Gateway에서 사전 체크 용도로 활용
            - 빠른 응답을 위한 경량화된 API
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정책 존재 여부 확인 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/policy/check/{clientId}/{apiPath}/{httpMethod}")
    public Mono<ResponseStdVO<Boolean>> checkEncryptionPolicyExists(
            @PathVariable @Parameter(description = "클라이언트 ID", required = true) String clientId,
            @PathVariable @Parameter(description = "API 경로", required = true) String apiPath,
            @PathVariable @Parameter(description = "HTTP 메서드", required = true) String httpMethod) {
        
        log.debug("암호화 정책 존재 여부 확인. clientId=[{}], apiPath=[{}], method=[{}]", 
                clientId, apiPath, httpMethod);
        
        EncryptionRequestDTO requestDTO = new EncryptionRequestDTO(clientId, apiPath, httpMethod);
        
        return encryptionPolicyService.getEncryptionPolicy(requestDTO)
                .map(response -> {
                    boolean exists = response.data() != null && 
                                   !response.data().encryptionFields().isEmpty();
                    
                    return new ResponseStdVO<>(response.trtErrInfoDTO(), exists);
                });
    }
}