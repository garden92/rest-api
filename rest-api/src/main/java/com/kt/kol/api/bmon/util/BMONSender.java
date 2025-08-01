package com.kt.kol.api.bmon.util;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

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
public class BMONSender {

    @Value("${server.bmon}")
    boolean bmonTrtFlag;

    @Value("${spring.profiles.active}")
    String onProfile;

    @Value("${spring.application.name}")
    String apiSrcName;

    private static final String HEADER_SEPARATOR = "=";
    private static final String LINE_FEED = "\n";

    private final ObjectMapper objectMapper;

    /**
     * BMON 연동 처리
     * 
     * @throws KolBusinessException
     */
    public <T> Mono<Void> sendBmonMot(String TrFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request) {

        return Mono.fromRunnable(() -> {

            // local 환경에서는 bmon연동 안함
            if ("local".equals(onProfile)) {
                log.debug("{}", "BMON Local Skip!");
                return;
            }

            // bmon flag 처리
            if (!bmonTrtFlag) {
                log.debug("{}", "BMON Flag is false. Skip!");
                return;
            }

            /**
             * Header 처리
             */
            JSONObject bmonHeader = new JSONObject();
            bmonHeader.put("appName", Constants.APP_NAME);
            bmonHeader.put("svcName", request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_URI));
            bmonHeader.put("fnName", "service");
            bmonHeader.put("globalNo", request.getHeaders().getFirst(HeaderConstants.HEADER_GLOBAL_NO));
            bmonHeader.put("chnlType", request.getHeaders().getFirst(HeaderConstants.HEADER_CHNL_TYPE));

            bmonHeader.put("trFlag", TrFlag);
            bmonHeader.put("trDate", DateUtil.Date_yyyyMMdd());
            bmonHeader.put("trTime", DateUtil.Date_HHmmssSSS());
            bmonHeader.put("clntIp", request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_IP));
            bmonHeader.put("responseType", trtErrInfoDTO.responseType());
            bmonHeader.put("responseCode", trtErrInfoDTO.responseCode());
            bmonHeader.put("responseTitle", "");
            bmonHeader.put("responseBasc", trtErrInfoDTO.responseBasc());
            bmonHeader.put("responseDtal", trtErrInfoDTO.responseDtal());
            bmonHeader.put("userId", request.getHeaders().getFirst(HeaderConstants.HEADER_USER_ID));
            bmonHeader.put("orgId", request.getHeaders().getFirst(HeaderConstants.HEADER_ORG_ID));
            bmonHeader.put("srcId", apiSrcName);
            bmonHeader.put("lgDateTime", request.getHeaders().getFirst(HeaderConstants.HEADER_LG_DATE_TIME));
            bmonHeader.put("cmpnCd", request.getHeaders().getFirst(HeaderConstants.HEADER_CMPN_CD));
            bmonHeader.put("curHostId", StringUtil.getIPAddress());

            // header key/value로 변환
            StringBuilder headerStrBulder = new StringBuilder(512); // 초기 용량 설정으로 resize 방지
            for (Object key : bmonHeader.keySet()) {
                headerStrBulder.append(key)
                        .append(HEADER_SEPARATOR)
                        .append(bmonHeader.get((String) key))
                        .append(LINE_FEED);
            }

            log.debug("BMON 연동 시작. 입력헤더=[{}]", headerStrBulder.toString());

            /*
             * 2025.04.28 json전문 연동은 bmon에서 마스킹 불가. Key:Value 형식으로 변경
             * //body json String 변환
             * String bodyString = "";
             * try {
             * ObjectMapper objMapper = new ObjectMapper();
             * 
             * //T인경우 DTO만, R인경우 VO로 처리한다.
             * if("T".equals(TrFlag)) {
             * bodyString =
             * objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inDTO);
             * } else {
             * RequestStdVO<T> reqVO = new RequestStdVO<T>(trtErrInfoDTO, inDTO);
             * bodyString =
             * objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reqVO);
             * }
             * } catch (JsonProcessingException e) {
             * //BMON연동은 오류 처리 없음.
             * log.error("BMON 메세지변환 오류 발생>{}", e.toString());
             * }
             */

            /**
             * Body 처리
             * Exception이 발생한 경우, inDTO는 Null로 입력됨.
             */
            String bodyString = "";
            if (inDTO != null) {
                // body json Key:Value형태 문자열로 변환
                String objMapperStr = "";
                try {
                    if ("T".equals(TrFlag)) {
                        objMapperStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inDTO);
                    } else {
                        RequestStdVO<T> reqVO = new RequestStdVO<T>(trtErrInfoDTO, inDTO);
                        objMapperStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reqVO);
                    }
                } catch (Exception e) {
                    log.warn("BMON JSON 변환 중 오류 발생. BMON 연동은 계속 진행됩니다. 오류: {}", e.getMessage(), e);
                    objMapperStr = "{}"; // 빈 JSON 객체로 fallback
                }

                try {
                    bodyString = jsonToKeyValue(new JSONObject(objMapperStr), "");
                } catch (Exception e) {
                    log.warn("BMON JSON 파싱 중 오류 발생. 빈 문자열로 처리합니다. 오류: {}", e.getMessage());
                    bodyString = "";
                }
            }

            log.debug("BMON 연동 시작. 입력전문=[{}]", bodyString);

            CommonPayloadCollector.sendPayloadKeyValue(Constants.LOG_POINT, headerStrBulder.toString(), bodyString);

        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // JSONObject to Key:Value
    public static String jsonToKeyValue(JSONObject inJsonObj, String prefix) {

        StringBuilder result = new StringBuilder(256); // 초기 용량 설정
        Iterator<String> keys = inJsonObj.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = inJsonObj.get(key);

            // 레벨구조로 찍으면 마스킹 처리 안됨...
            // String currKey = prefix.isEmpty() ? key : prefix + "." + key;
            String currKey = key;

            if (value instanceof JSONObject) {
                result.append(currKey)
                        .append(":")
                        .append("")
                        .append("\n");
                result.append(jsonToKeyValue((JSONObject) value, currKey));
            } else if (value instanceof JSONArray) {
                result.append(currKey)
                        .append(":")
                        .append("")
                        .append("\n");
                result.append(processJsonArray((JSONArray) value, currKey));
            } else {
                result.append(currKey)
                        .append(":")
                        .append(value)
                        .append("\n");
            }
        }

        return result.toString();
    }

    // JSONArray to key:Value
    public static String processJsonArray(JSONArray jsonArray, String prefix) {

        StringBuilder result = new StringBuilder(128); // 초기 용량 설정

        for (int i = 0; i < jsonArray.length(); i++) {
            Object item = jsonArray.get(i);
            String currKey = prefix + "[" + i + "]";

            if (item instanceof JSONObject) {
                result.append(currKey)
                        .append(":")
                        .append("")
                        .append("\n");
                result.append(jsonToKeyValue((JSONObject) item, currKey));
            } else if (item instanceof JSONArray) {
                result.append(currKey)
                        .append(":")
                        .append("")
                        .append("\n");
                result.append(processJsonArray((JSONArray) item, currKey));
            } else {
                result.append(currKey)
                        .append(":")
                        .append(item)
                        .append("\n");
            }

        }

        return result.toString();
    }
}
