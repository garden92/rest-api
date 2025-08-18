package com.kt.kol.api.encryption.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kt.kol.api.encryption.model.EcodPlcyHstDTO;
import com.kt.kol.api.encryption.model.EcodPlcyResponseDTO;
import com.kt.kol.api.encryption.service.EcodPlcyHstService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 암호화 정책 이력 API 컨트롤러
 * 키는 KeyVault에서 관리하므로 정책 조회만 제공
 */
@RestController
@RequestMapping("/encryption/policy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "암호화 정책 이력 API", description = "암호화 정책 이력 관리 API")
public class EcodPlcyHstController {

	private final EcodPlcyHstService ecodPlcyHstService;

	/**
	 * 현재 유효한 암호화 정책 조회
	 */
	@GetMapping("/current/{chId}/{fieldNm}")
	@Operation(summary = "현재 유효한 암호화 정책 조회", description = "채널 ID와 필드명으로 현재 유효한 암호화 정책을 조회합니다.")
	public Mono<ResponseEntity<EcodPlcyResponseDTO>> getCurrentValidPolicy(
			@Parameter(description = "채널 ID", example = "AI") @PathVariable String chId,
			@Parameter(description = "필드명", example = "user_name") @PathVariable String fieldNm) {

		log.info("현재 유효한 암호화 정책 조회 요청 - 채널: {}, 필드: {}", chId, fieldNm);

		return ecodPlcyHstService.getCurrentValidPolicy(chId, fieldNm)
				.map(EcodPlcyResponseDTO::from)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build())
				.doOnNext(response -> log.info("암호화 정책 조회 완료 - 상태: {}", response.getStatusCode()))
				.doOnError(error -> log.error("암호화 정책 조회 중 오류 발생", error));
	}

	/**
	 * 채널의 모든 현재 유효한 정책 조회
	 */
	@GetMapping("/current/{chId}")
	@Operation(summary = "채널의 현재 유효한 모든 정책 조회", description = "특정 채널의 현재 유효한 모든 암호화 정책을 조회합니다.")
	public Flux<EcodPlcyResponseDTO> getAllCurrentValidPoliciesByChannel(
			@Parameter(description = "채널 ID", example = "AI") @PathVariable String chId) {

		log.info("채널의 모든 현재 유효한 정책 조회 요청 - 채널: {}", chId);

		return ecodPlcyHstService.getAllCurrentValidPoliciesByChannel(chId)
				.map(EcodPlcyResponseDTO::from)
				.doOnComplete(() -> log.info("채널 정책 조회 완료 - 채널: {}", chId))
				.doOnError(error -> log.error("채널 정책 조회 중 오류 발생 - 채널: {}", chId, error));
	}

	/**
	 * 모든 현재 유효한 정책 조회
	 */
	@GetMapping("/current")
	@Operation(summary = "모든 현재 유효한 정책 조회", description = "모든 채널의 현재 유효한 암호화 정책을 조회합니다.")
	public Flux<EcodPlcyResponseDTO> getAllCurrentValidPolicies() {

		log.info("모든 현재 유효한 정책 조회 요청");

		return ecodPlcyHstService.getAllCurrentValidPolicies()
				.map(EcodPlcyResponseDTO::from)
				.doOnComplete(() -> log.info("전체 정책 조회 완료"))
				.doOnError(error -> log.error("전체 정책 조회 중 오류 발생", error));
	}

	/**
	 * 정책 이력 조회
	 */
	@GetMapping("/history/{chId}/{fieldNm}")
	@Operation(summary = "정책 이력 조회", description = "특정 채널과 필드의 암호화 정책 이력을 조회합니다.")
	public Flux<EcodPlcyResponseDTO> getPolicyHistory(
			@Parameter(description = "채널 ID", example = "AI") @PathVariable String chId,
			@Parameter(description = "필드명", example = "user_name") @PathVariable String fieldNm) {

		log.info("정책 이력 조회 요청 - 채널: {}, 필드: {}", chId, fieldNm);

		return ecodPlcyHstService.getPolicyHistory(chId, fieldNm)
				.map(EcodPlcyResponseDTO::from)
				.doOnComplete(() -> log.info("정책 이력 조회 완료 - 채널: {}, 필드: {}", chId, fieldNm))
				.doOnError(error -> log.error("정책 이력 조회 중 오류 발생", error));
	}

	/**
	 * 암호화 필요 여부 확인
	 */
	@GetMapping("/check/{chId}/{fieldNm}")
	@Operation(summary = "암호화 필요 여부 확인", description = "특정 필드가 암호화 대상인지 확인합니다.")
	public Mono<ResponseEntity<Map<String, Object>>> checkEncryptionRequired(
			@Parameter(description = "채널 ID", example = "AI") @PathVariable String chId,
			@Parameter(description = "필드명", example = "user_name") @PathVariable String fieldNm) {

		log.info("암호화 필요 여부 확인 요청 - 채널: {}, 필드: {}", chId, fieldNm);

		return ecodPlcyHstService.isEncryptionRequired(chId, fieldNm)
				.map(required -> {
					Map<String, Object> response = Map.of(
							"chId", chId,
							"fieldNm", fieldNm,
							"encryptionRequired", required,
							"checkTime", LocalDateTime.now().toString());
					return ResponseEntity.ok(response);
				})
				.doOnNext(response -> log.info("암호화 필요 여부 확인 완료 - 채널: {}, 필드: {}", chId, fieldNm))
				.doOnError(error -> log.error("암호화 필요 여부 확인 중 오류 발생", error));
	}

	/**
	 * 채널의 암호화 필드 목록 조회
	 */
	@GetMapping("/fields/{chId}")
	@Operation(summary = "채널의 암호화 필드 목록 조회", description = "특정 채널에서 암호화가 필요한 필드 목록을 조회합니다.")
	public Mono<ResponseEntity<java.util.List<String>>> getEncryptionFieldNames(
			@Parameter(description = "채널 ID", example = "AI") @PathVariable String chId) {

		log.info("암호화 필드 목록 조회 요청 - 채널: {}", chId);

		return ecodPlcyHstService.getEncryptionFieldNames(chId)
				.collectList()
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.ok(java.util.List.of()))
				.doOnNext(response -> log.info("암호화 필드 목록 조회 완료 - 채널: {}, 필드 수: {}", chId,
						response.getBody() != null ? response.getBody().size() : 0))
				.doOnError(error -> log.error("암호화 필드 목록 조회 중 오류 발생 - 채널: {}", chId, error));
	}
}