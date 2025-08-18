package com.kt.kol.api.prechk.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.kt.kol.api.bmon.util.BmonSenderInterface;
import com.kt.kol.api.prechk.model.ApiKeyInfoInfoDTO;
import com.kt.kol.api.prechk.model.DummyDTO;
import com.kt.kol.api.prechk.repository.ApiKeyInfoRepository;
import com.kt.kol.api.prechk.repository.KolChInfoRepository;
import com.kt.kol.api.prechk.repository.KolChIpInfoRepository;
import com.kt.kol.api.prechk.repository.KolChUserInfoRepository;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.HeaderConstants;
import com.kt.kol.common.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestGwPreChkService {

    private final KolChInfoRepository kolChInfoRepository;
    private final KolChIpInfoRepository kolChIpInfoRepository;
    private final KolChUserInfoRepository kolChUserInfoRepository;
    private final ApiKeyInfoRepository apiKeyInfoRepository;

    private final BmonSenderInterface bmonSender;

    // restGW 라우팅 전 사전체크
    public <T> Mono<ResponseStdVO<DummyDTO>> checkBeforeRoute(T inDTO, ServerWebExchange exchange) {

        // 체크 수행 전 요청 BMON 로그 적재
        return bmonSender.sendBmonMot("T", inDTO, new TrtErrInfoDTO("I", "", "", ""), exchange.getRequest())
                .then(Mono.defer(() -> this.preChcker(exchange)));
    }

    public <T> Mono<ResponseStdVO<DummyDTO>> checkBeforeRoute(ServerWebExchange exchange) {
        return this.preChcker(exchange);

    }

    public <T> Mono<ResponseStdVO<DummyDTO>> preChcker(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();

        // 날짜 형식 유효성 체크
        if (!isValidDateTime(request.getHeaders().getFirst(HeaderConstants.HEADER_LG_DATE_TIME))) {
            return errorBmonSend("유효하지 않은 날짜 형식[KOL-Lg-Date-Time] 입니다.", request);
        }

        // 채널ID 존재 여부만 체크 - hasElements() 사용으로 메모리 효율 확보
        String requestUri = request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_URI);
        Mono<Boolean> chChkMono = kolChInfoRepository
                .checkKolChInfo(request.getHeaders().getFirst(HeaderConstants.HEADER_CHNL_TYPE),
                        request.getHeaders().getFirst(HeaderConstants.HEADER_LG_DATE_TIME))
                .any(ch -> requestUri.startsWith(ch.chPathAdr())); // 경로 체크도 함께 수행

        // 허용IP 체크 - 존재 여부만 확인
        Mono<Boolean> ipChkMono = kolChIpInfoRepository
                .checkKolChIpInfo(request.getHeaders().getFirst(HeaderConstants.HEADER_CHNL_TYPE),
                        request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_IP))
                .hasElements();

        // 허용사용자 체크 - 존재 여부만 확인
        Mono<Boolean> userChkMono = kolChUserInfoRepository
                .checkKolChUserInfo(request.getHeaders().getFirst(HeaderConstants.HEADER_CHNL_TYPE),
                        request.getHeaders().getFirst(HeaderConstants.HEADER_USER_ID))
                .hasElements();

        // API Key 체크 - 실제 값이 필요하므로 next() 유지
        Mono<ApiKeyInfoInfoDTO> apiKeyChkMono = apiKeyInfoRepository
                .checkApiKeyInfo(request.getHeaders().getFirst(HeaderConstants.HEADER_CHNL_TYPE),
                        request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_URI),
                        request.getHeaders().getFirst(HeaderConstants.HEADER_LG_DATE_TIME),
                        request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_IP))
                .next()
                .switchIfEmpty(Mono.just(new ApiKeyInfoInfoDTO(null, null, null)));

        // 병렬 체크 로직 수행
        return Mono.zip(chChkMono, ipChkMono, userChkMono, apiKeyChkMono)
                .flatMap(dbRslt -> {

                    /**
                     * 결과 처리
                     * 1. 채널ID 및 채널허용Path (Boolean)
                     * 2. 채널IP (Boolean)
                     * 3. 채널UserID (Boolean)
                     * 4. API Key (DTO)
                     */
                    TrtErrInfoDTO err = new TrtErrInfoDTO("I", "", "", "");

                    /* 1. 채널ID 및 경로 체크 */
                    Boolean chChkResult = dbRslt.getT1();
                    if (!chChkResult) {
                        log.debug("허용 되지 않는 ChnlType 또는 서비스URL 입니다. ChnlType=[{}], URL=[{}]",
                                request.getHeaders().getFirst(HeaderConstants.HEADER_CHNL_TYPE),
                                requestUri);
                        return errorBmonSend("허용 되지 않는 ChnlType 또는 서비스URL 입니다.", request);
                    }

                    /* 2. 채널IP 체크 */
                    Boolean ipChkResult = dbRslt.getT2();
                    if (!ipChkResult) {
                        log.debug("허용 되지 않는 IP 입니다. = [{}]",
                                request.getHeaders().getFirst(HeaderConstants.HEADER_ORI_IP));
                        return errorBmonSend("허용 되지 않는 IP 입니다.", request);
                    }

                    /* 3. 채널사용자 체크 */
                    Boolean userChkResult = dbRslt.getT3();
                    if (!userChkResult) {
                        log.debug("허용 되지 않는 사용자ID 입니다. = [{}]",
                                request.getHeaders().getFirst(HeaderConstants.HEADER_USER_ID));
                        return errorBmonSend("허용 되지 않는 사용자ID 입니다.", request);
                    }

                    /* 4. API Key 체크 */
                    ApiKeyInfoInfoDTO apiKeyChkResult = dbRslt.getT4();
                    if (StringUtil.isNull(apiKeyChkResult.chId())) {
                        log.debug("API Key 조회 결과 없음.");
                        return errorBmonSend("API Key 조회 결과 없음. 시스템 관리자에게 문의 하세요.", request);
                    }

                    // API Key 인증 체크
                    Mono<ResponseStdVO<DummyDTO>> apiKeyValidation = validateApiKey(request,
                            apiKeyChkResult);
                    if (apiKeyValidation != null) {
                        return apiKeyValidation;
                    }

                    return Mono.just(new ResponseStdVO<DummyDTO>(err, new DummyDTO()));
                });
    }

    private boolean isValidDateTime(String dateTimeStr) {
        if (StringUtil.isNull(dateTimeStr)) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime.parse(dateTimeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private Mono<ResponseStdVO<DummyDTO>> validateApiKey(ServerHttpRequest request, ApiKeyInfoInfoDTO apiKeyInfo) {
        String apiKeyValue = apiKeyInfo.apiKeyVal();

        // DB에 *(ALL)로 등록되어있는 경우에는 체크 SKIP
        if ("*".equals(apiKeyValue)) {
            log.debug("API Key 전체 허용 서비스로 인증 SKIP!");
            return null;
        }

        String headerApiKey = request.getHeaders().getFirst(HeaderConstants.HEADER_AUTH_KEY);

        if (StringUtil.isNull(headerApiKey)) {
            log.debug("API Key가 입력되지 않았습니다.");
            return errorBmonSend("API Key가 입력되지 않았습니다.", request);
        }

        if (!headerApiKey.startsWith("Bearer ")) {
            log.debug("API Key유형이 Bearer Type이 아닙니다.");
            return errorBmonSend("API Key유형이 Bearer Type이 아닙니다.", request);
        }

        String actualApiKey = headerApiKey.substring(7);
        if (!actualApiKey.equals(apiKeyValue)) {
            log.debug("API Key 인증에 실패 하였습니다.");
            return errorBmonSend("API Key 인증에 실패 하였습니다.", request);
        }

        return null;
    }

    // 오류 BMON 처리 후 return
    public Mono<ResponseStdVO<DummyDTO>> errorBmonSend(String responseBasc, ServerHttpRequest request) {

        TrtErrInfoDTO errInfoDto = new TrtErrInfoDTO("E", "KOLE0001", responseBasc, "");
        ResponseStdVO<DummyDTO> outVO = new ResponseStdVO<DummyDTO>(errInfoDto, new DummyDTO());

        return bmonSender.sendBmonMot("R", new DummyDTO(), errInfoDto, request)
                .thenReturn(outVO);
    }

}
