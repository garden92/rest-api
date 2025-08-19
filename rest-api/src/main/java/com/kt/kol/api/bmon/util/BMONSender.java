package com.kt.kol.api.bmon.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
// @Profile({ "dev", "sit", "prd" })
@Profile({ "mock" })
public class BMONSender implements BmonSenderInterface {

    @Value("${server.bmon}")
    boolean bmonTrtFlag;

    @Value("${spring.application.name}")
    String apiSrcName;

    private static final String HEADER_KV_SEP = "="; // 헤더 key=value
    private static final String BODY_KV_SEP = ":"; // 바디 key:value
    private static final String LINE_FEED = "\n"; // 실제 개행(문자 그대로 \n 아님)

    private static final int INITIAL_HEADER_CAPACITY = 512;
    private static final int INITIAL_BODY_CAPACITY = 1024;

    private static final TypeReference<Map<String, Object>> MAP_TYPEREF = new TypeReference<Map<String, Object>>() {
    };

    private final ObjectMapper objectMapper;

    /**
     * BMON 연동 처리
     *
     * @param trFlag        T: 원문 그대로(inDTO) / 그 외: RequestStdVO로 감싸서 전송
     * @param inDTO         입력 바디(또는 표준 래핑 대상)
     * @param trtErrInfoDTO 응답/오류 정보(옵션)
     * @param request       요청 정보/헤더
     */
    @Override
    public <T> Mono<Void> sendBmonMot(String trFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request) {
        if (!bmonTrtFlag) {
            log.debug("BMON Flag is false. Skip!");
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            String headerString = buildBmonHeader(trFlag, trtErrInfoDTO, request);
            String bodyString = buildBmonBody(trFlag, inDTO, trtErrInfoDTO);

            // 운영 로그는 과다 노출 주의. 필요 시 마스킹 로직 추가 권장.
            log.debug("BMON 연동 시작. 헤더=[{}], 바디=[{}]", headerString, bodyString);

            CommonPayloadCollector.sendPayloadKeyValue(Constants.LOG_POINT, headerString, bodyString);
        })
                .subscribeOn(Schedulers.boundedElastic()) // 블로킹 가능성 대비
                .onErrorResume(ex -> {
                    log.warn("BMON 전송 실패: {}", ex.toString());
                    // TODO: meter/카운터 등록
                    return Mono.empty();
                })
                .then();
    }

    /** BMON Header 구성 */
    private String buildBmonHeader(String trFlag, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request) {
        Map<String, Object> h = new LinkedHashMap<>(24);

        h.put("appName", Constants.APP_NAME);
        h.put("svcName", getHeaderValue(request, HeaderConstants.HEADER_ORI_URI));
        h.put("fnName", "service");
        h.put("globalNo", getHeaderValue(request, HeaderConstants.HEADER_GLOBAL_NO));
        h.put("chnlType", getHeaderValue(request, HeaderConstants.HEADER_CHNL_TYPE));
        h.put("trFlag", nvl(trFlag));
        h.put("trDate", DateUtil.Date_yyyyMMdd());
        h.put("trTime", DateUtil.Date_HHmmssSSS());
        h.put("clntIp", getHeaderValue(request, HeaderConstants.HEADER_ORI_IP));

        String respType = trtErrInfoDTO == null ? "" : nvl(trtErrInfoDTO.responseType());
        String respCode = trtErrInfoDTO == null ? "" : nvl(trtErrInfoDTO.responseCode());
        String respBasc = trtErrInfoDTO == null ? "" : nvl(trtErrInfoDTO.responseBasc());
        String respDtal = trtErrInfoDTO == null ? "" : nvl(trtErrInfoDTO.responseDtal());

        h.put("responseType", respType);
        h.put("responseCode", respCode);
        h.put("responseTitle", "");
        h.put("responseBasc", respBasc);
        h.put("responseDtal", respDtal);

        h.put("userId", getHeaderValue(request, HeaderConstants.HEADER_USER_ID));
        h.put("orgId", getHeaderValue(request, HeaderConstants.HEADER_ORG_ID));
        h.put("srcId", apiSrcName);
        h.put("lgDateTime", getHeaderValue(request, HeaderConstants.HEADER_LG_DATE_TIME));
        h.put("cmpnCd", getHeaderValue(request, HeaderConstants.HEADER_CMPN_CD));
        h.put("curHostId", StringUtil.getIPAddress());

        return toKeyValueLines(h, HEADER_KV_SEP, INITIAL_HEADER_CAPACITY);
    }

    /** BMON Body 구성 */
    private <T> String buildBmonBody(String trFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO) {
        if (inDTO == null)
            return "";
        try {
            Object target = "T".equals(trFlag) ? inDTO : new RequestStdVO<>(trtErrInfoDTO, inDTO);
            return objectToKeyValueString(target);
        } catch (Exception e) {
            log.warn("BMON Body 생성 오류: {}", e.getMessage(), e);
            return "";
        }
    }

    /** 안전한 헤더 값 조회 */
    private String getHeaderValue(ServerHttpRequest request, String headerName) {
        String v = request.getHeaders().getFirst(headerName);
        return v == null ? "" : v;
    }

    /** null -> "" */
    private String nvl(String s) {
        return s == null ? "" : s;
    }

    /** 값 sanitize: 개행 제거/구분자 이스케이프 */
    private String sanitize(String v) {
        if (v == null)
            return "";
        v = v.replace("\r", " ").replace("\n", " ");
        // 파서 안전을 위해 구분자 이스케이프(필요 시 규칙 조정)
        v = v.replace("=", "\\=").replace(":", "\\:");
        return v;
    }

    /** Map -> key{sep}value\n */
    private String toKeyValueLines(Map<String, ?> map, String sep, int initialCapacity) {
        StringBuilder sb = new StringBuilder(initialCapacity);
        map.forEach((k, v) -> {
            sb.append(k)
                    .append(sep)
                    .append(sanitize(String.valueOf(v == null ? "" : v)))
                    .append(LINE_FEED);
        });
        return sb.toString();
    }

    /** 객체를 플랫 Key:Value 문자열로 변환 (Map/JSON/String 모두 수용) */
    private <T> String objectToKeyValueString(T object) throws Exception {
        if (object == null)
            return "";

        // 문자열이면 JSON 파싱 시도
        if (object instanceof String s) {
            try {
                Map<String, Object> map = objectMapper.readValue(s, MAP_TYPEREF);
                Map<String, Object> flat = flattenMap(map, "");
                return toBodyLines(flat);
            } catch (Exception parseFail) {
                // JSON 아님 → 원문 sanitize 후 반환
                return sanitize(s);
            }
        }

        // 그 외는 Map으로 변환 후 플래트닝
        Map<String, Object> map = objectMapper.convertValue(object, MAP_TYPEREF);
        Map<String, Object> flat = flattenMap(map, "");
        return toBodyLines(flat);
    }

    /** 바디 전용 출력(builder 재사용 규격) */
    private String toBodyLines(Map<String, Object> flat) {
        StringBuilder sb = new StringBuilder(INITIAL_BODY_CAPACITY);
        flat.forEach((k, v) -> {
            sb.append(k)
                    .append(BODY_KV_SEP)
                    .append(sanitize(String.valueOf(v == null ? "" : v)))
                    .append(LINE_FEED);
        });
        return sb.toString();
    }

    /** Map 플래트닝: a.b[0].c 형태로 키 구성 (키 충돌 방지/계층 보존) */
    private Map<String, Object> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>(map.size() * 2);
        String base = (prefix == null || prefix.isEmpty()) ? "" : prefix + ".";
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = base + e.getKey();
            Object val = e.getValue();
            if (val instanceof Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) nested;
                result.putAll(flattenMap(cast, key));
            } else if (val instanceof List<?> list) {
                result.putAll(flattenList(list, key));
            } else {
                result.put(key, val == null ? "" : val);
            }
        }
        return result;
    }

    /** List 플래트닝: a.b[0], a.b[1] ... */
    private Map<String, Object> flattenList(List<?> list, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>(list.size() * 2);
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            String key = prefix + "[" + i + "]";
            if (item instanceof Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) nested;
                result.putAll(flattenMap(cast, key));
            } else if (item instanceof List<?> nestedList) {
                result.putAll(flattenList(nestedList, key));
            } else {
                result.put(key, item == null ? "" : item);
            }
        }
        return result;
    }
}