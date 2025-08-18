package com.kt.kol.api.bmon.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.kt.icis.cmmnfrwk.monitoring.api.CommonPayloadCollector;
import com.kt.kol.common.model.RequestStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.Constants;
import com.kt.kol.common.util.DateUtil;
import com.kt.kol.common.util.HeaderConstants;
import com.kt.kol.common.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile({ "dev", "sit", "prd" })
public class BMONSender implements BmonSenderInterface {

    @Value("${server.bmon}")
    boolean bmonTrtFlag;

    @Value("${spring.profiles.active}")
    String onProfile;

    @Value("${spring.application.name}")
    String apiSrcName;

    private static final String HEADER_SEPARATOR = "=";
    private static final String LINE_FEED = "\\n";
    private static final String EMPTY_JSON = "{}";
    private static final int INITIAL_HEADER_CAPACITY = 512;
    private static final int INITIAL_BODY_CAPACITY = 1024;

    private final ObjectMapper objectMapper;

    /**
     * BMON 연동 처리 (WebFlux 친화적 버전)
     */
    @Override
    public <T> Mono<Void> sendBmonMot(String TrFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request) {
        // 조기 반환으로 불필요한 처리 방지
        if (!bmonTrtFlag) {
            log.debug("BMON Flag is false. Skip!");
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            String headerString = buildBmonHeader(TrFlag, trtErrInfoDTO, request);

            // Body 구성
            String bodyString = buildBmonBody(TrFlag, inDTO, trtErrInfoDTO);

            log.debug("BMON 연동 시작. 입력헤더=[{}], 입력전문=[{}]", headerString, bodyString);

            // BMON 전송
            CommonPayloadCollector.sendPayloadKeyValue(Constants.LOG_POINT, headerString, bodyString);

        }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(ex -> {
                    log.warn("BMON 전송 실패: {}", ex.toString());
                    // TODO meter/카운터
                    return Mono.empty();
                })
                .then();
    }

    /**
     * BMON Header 구성 (최적화된 버전)
     */
    private String buildBmonHeader(String TrFlag, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request) {
        // LinkedHashMap 사용으로 순서 보장 및 성능 개선
        Map<String, Object> bmonHeader = new LinkedHashMap<>(20);

        // 필수 헤더 구성
        bmonHeader.put("appName", Constants.APP_NAME);
        bmonHeader.put("svcName", getHeaderValue(request, HeaderConstants.HEADER_ORI_URI));
        bmonHeader.put("fnName", "service");
        bmonHeader.put("globalNo", getHeaderValue(request, HeaderConstants.HEADER_GLOBAL_NO));
        bmonHeader.put("chnlType", getHeaderValue(request, HeaderConstants.HEADER_CHNL_TYPE));
        bmonHeader.put("trFlag", TrFlag);
        bmonHeader.put("trDate", DateUtil.Date_yyyyMMdd());
        bmonHeader.put("trTime", DateUtil.Date_HHmmssSSS());
        bmonHeader.put("clntIp", getHeaderValue(request, HeaderConstants.HEADER_ORI_IP));
        bmonHeader.put("responseType", trtErrInfoDTO.responseType());
        bmonHeader.put("responseCode", trtErrInfoDTO.responseCode());
        bmonHeader.put("responseTitle", "");
        bmonHeader.put("responseBasc", trtErrInfoDTO.responseBasc());
        bmonHeader.put("responseDtal", trtErrInfoDTO.responseDtal());
        bmonHeader.put("userId", getHeaderValue(request, HeaderConstants.HEADER_USER_ID));
        bmonHeader.put("orgId", getHeaderValue(request, HeaderConstants.HEADER_ORG_ID));
        bmonHeader.put("srcId", apiSrcName);
        bmonHeader.put("lgDateTime", getHeaderValue(request, HeaderConstants.HEADER_LG_DATE_TIME));
        bmonHeader.put("cmpnCd", getHeaderValue(request, HeaderConstants.HEADER_CMPN_CD));
        bmonHeader.put("curHostId", StringUtil.getIPAddress());

        return convertToKeyValueString(bmonHeader, INITIAL_HEADER_CAPACITY);
    }

    /**
     * BMON Body 구성 (최적화된 버전)
     */
    private <T> String buildBmonBody(String TrFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO) {
        if (inDTO == null) {
            return "";
        }

        try {
            Object targetObject = "T".equals(TrFlag) ? inDTO : new RequestStdVO<>(trtErrInfoDTO, inDTO);
            return objectToKeyValueString(targetObject);
        } catch (Exception e) {
            log.warn("BMON Body 생성 중 오류 발생: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Header 값 안전하게 가져오기
     */
    private String getHeaderValue(ServerHttpRequest request, String headerName) {
        String value = request.getHeaders().getFirst(headerName);
        return value != null ? value : "";
    }

    /**
     * Map을 Key:Value 문자열로 변환 (최적화된 버전)
     */
    private String convertToKeyValueString(Map<String, Object> map, int initialCapacity) {
        StringBuilder builder = new StringBuilder(initialCapacity);
        map.forEach((key, value) -> builder.append(key).append(HEADER_SEPARATOR).append(value).append(LINE_FEED));
        return builder.toString();
    }

    /**
     * 객체를 Key:Value 형태로 변환 (단일 변환으로 최적화)
     */
    private <T> String objectToKeyValueString(T object) throws Exception {
        if (object == null) {
            return "";
        }

        // Jackson으로 직접 Map 변환 (이중 변환 제거)
        Map<String, Object> map = objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});

        // 플랫 맵으로 변환
        Map<String, Object> flatMap = flattenMapOptimized(map, "");

        // Key:Value 문자열 생성
        StringBuilder result = new StringBuilder(INITIAL_BODY_CAPACITY);
        flatMap.forEach((key, value) -> result.append(key).append(":").append(value).append(LINE_FEED));

        return result.toString();
    }

    /**
     * 고성능 플래튼 변환 (GC 최적화)
     */
    private Map<String, Object> flattenMapOptimized(Map<String, Object> map, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>(map.size() * 2); // 초기 용량 최적화

        map.forEach((key, value) -> {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                result.put(key, "");
                result.putAll(flattenMapOptimized(nestedMap, key));
            } else if (value instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) value;
                result.put(key, "");
                result.putAll(flattenListOptimized(list, key));
            } else {
                result.put(key, value != null ? value : "");
            }
        });

        return result;
    }

    /**
     * 고성능 리스트 플래튼 (GC 최적화)
     */
    private Map<String, Object> flattenListOptimized(java.util.List<Object> list, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>(list.size() * 2);

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            String key = prefix + "[" + i + "]";

            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) item;
                result.put(key, "");
                result.putAll(flattenMapOptimized(nestedMap, key));
            } else if (item instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> nestedList = (java.util.List<Object>) item;
                result.put(key, "");
                result.putAll(flattenListOptimized(nestedList, key));
            } else {
                result.put(key, item != null ? item : "");
            }
        }

        return result;
    }
}