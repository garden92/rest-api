package com.kt.kol.api.prechk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kt.kol.api.bmon.util.BMONSender;
import com.kt.kol.api.prechk.model.ApiKeyInfoInfoDTO;
import com.kt.kol.api.prechk.model.DummyDTO;
import com.kt.kol.api.prechk.model.KolChInfoDTO;
import com.kt.kol.api.prechk.model.KolChIpInfoDTO;
import com.kt.kol.api.prechk.model.KolChUserInfoDTO;
import com.kt.kol.api.prechk.repository.ApiKeyInfoRepository;
import com.kt.kol.api.prechk.repository.KolChInfoRepository;
import com.kt.kol.api.prechk.repository.KolChIpInfoRepository;
import com.kt.kol.api.prechk.repository.KolChUserInfoRepository;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.HeaderConstants;
import com.kt.kol.common.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
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
	
	private final BMONSender bmonSender;
	
	//restGW 라우팅 전 사전체크
	public <T> Mono<ResponseStdVO<DummyDTO>> ckeckBeforeRoute(T inDTO, HttpServletRequest request) {
		
		//체크 수행 전 요청 BMON 로그 적재
		return bmonSender.sendBmonMot("T", inDTO, new TrtErrInfoDTO("I", "", "", ""), request)
				.then(Mono.defer(() -> {
					
					//채널ID 및 허용경로 체크
					Mono<List<KolChInfoDTO>> chChkList = kolChInfoRepository.checkKolChInfo(request.getHeader(HeaderConstants.HEADER_CHNL_TYPE)
																							, request.getHeader(HeaderConstants.HEADER_LG_DATE_TIME)).collectList();
															
					//허용IP 체크
					Mono<List<KolChIpInfoDTO>> ipChkList = kolChIpInfoRepository.checkKolChIpInfo(request.getHeader(HeaderConstants.HEADER_CHNL_TYPE)
																									, request.getRemoteAddr()).collectList();
															
					//허용사용자 체크
					Mono<List<KolChUserInfoDTO>> userChkList = kolChUserInfoRepository.checkKolChUserInfo(request.getHeader(HeaderConstants.HEADER_CHNL_TYPE)
																											, request.getHeader(HeaderConstants.HEADER_USER_ID)).collectList();
					//API Key 체크
					Mono<List<ApiKeyInfoInfoDTO>> apiKeyChkList = apiKeyInfoRepository.checkApiKeyInfo(request.getHeader(HeaderConstants.HEADER_CHNL_TYPE)
																										, request.getHeader(HeaderConstants.HEADER_ORI_URI)
																										, request.getHeader(HeaderConstants.HEADER_LG_DATE_TIME)
																										, request.getHeader(HeaderConstants.HEADER_API_KEY)).collectList();

					//병렬 체크 로직 수행
					return Mono.zip(chChkList, ipChkList, userChkList, apiKeyChkList)
							.flatMap(dbRslt -> {
								
								/**
								 * 결과 처리
								 * 1. 채널ID 및 채널허용Path
								 * 2. 채널IP
								 * 3. 채널UserID
								 * 4. API Key
								 */
								TrtErrInfoDTO err = new TrtErrInfoDTO("I", "", "", "");
								
								//1-1. 채널ID 체크
								List<KolChInfoDTO> chChkListRtn = dbRslt.getT1();
								if(chChkListRtn.size() <= 0 || StringUtil.isNull(chChkListRtn.get(0).chId())) {
									log.debug("허용 되지 않는 ChnlType 입니다. = [{}]", request.getHeader(HeaderConstants.HEADER_CHNL_TYPE));
									return errorBmonSend("허용 되지 않는 ChnlType 입니다.", request);
								} 

								//1-2. 채널path 체크
								boolean pathRslt = false;
								for(int i=0; i < chChkListRtn.size(); i++) {
									if(request.getHeader(HeaderConstants.HEADER_ORI_URI).startsWith(chChkListRtn.get(i).chPathAdr())) {
										pathRslt = true;
										break;
									}
								}
								if(!pathRslt) {
									log.debug("허용 되지 않는 서비스URL 입니다. = [{}]", request.getHeader(HeaderConstants.HEADER_ORI_URI));
									return errorBmonSend("허용 되지 않는 서비스URL 입니다.", request);
								}
								
								//2. 채널IP 체크
								List<KolChIpInfoDTO> chIpChkListRtn = dbRslt.getT2();
								if(chIpChkListRtn.size() <= 0 || StringUtil.isNull(chIpChkListRtn.get(0).chId())) {
									log.debug("허용 되지 않는 IP 입니다. = [{}]", request.getRemoteAddr());
									return errorBmonSend("허용 되지 않는 IP 입니다.", request);
								}
								
								//3. 채널사용자 체크
								List<KolChUserInfoDTO> chUserChkListRtn = dbRslt.getT3();
								if(chUserChkListRtn.size() <= 0 || StringUtil.isNull(chUserChkListRtn.get(0).chId())) {
									log.debug("허용 되지 않는 사용자ID 입니다. = [{}]", request.getHeader(HeaderConstants.HEADER_USER_ID));
									return errorBmonSend("허용 되지 않는 사용자ID 입니다.", request); 
								}

								//4. API Key 체크
								if(StringUtil.isNull(request.getHeader(HeaderConstants.HEADER_API_KEY))) {
									log.debug("API Key가 입력되지 않았습니다.");
									return errorBmonSend("API Key가 입력되지 않았습니다.", request);
								}

								List<ApiKeyInfoInfoDTO> apiKeyChkListRtn = dbRslt.getT4();
								if(apiKeyChkListRtn.size() <= 0 || StringUtil.isNull(apiKeyChkListRtn.get(0).chId())) {
									log.debug("API Key 인증에 실패 하였습니다.");
									return errorBmonSend("API Key 인증에 실패 하였습니다.", request); 
								}

								return Mono.just(new ResponseStdVO<DummyDTO>(err, new DummyDTO()));
							});
				}));
		
	}
	
	//오류 BMON 처리 후 return
	public Mono<ResponseStdVO<DummyDTO>> errorBmonSend(String responseBasc, HttpServletRequest request) {
		
		TrtErrInfoDTO errInfoDto = new TrtErrInfoDTO("E", "KOLE0001", responseBasc, "");
		ResponseStdVO<DummyDTO> outVO = new ResponseStdVO<DummyDTO>(errInfoDto, new DummyDTO());
		
		return bmonSender.sendBmonMot("R", new DummyDTO(), errInfoDto, request)
				.thenReturn(outVO);
	}
	
	
}
