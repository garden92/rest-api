package com.kt.kol.api.bmon.util;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.icis.cmmnfrwk.monitoring.api.CommonPayloadCollector;
import com.kt.kol.common.model.RequestStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.Constants;
import com.kt.kol.common.util.DateUtil;
import com.kt.kol.common.util.HeaderConstants;
import com.kt.kol.common.util.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class BMONSender {
	
	@Value("${server.bmon}")
	boolean bmonTrtFlag;
	
	@Value("${spring.profiles.active}")
	String onProfile;
	
	@Value("${spring.application.name}")
	String apiSrcName;

	String HEADER_SEPARATOR = "=";
	String LINE_FEED = "\n";
	
	/**
	 * BMON 연동 처리
	 * @throws KolBusinessException 
	 */
	public <T> Mono<Void> sendBmonMot(String TrFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO, HttpServletRequest request) {
		
		return Mono.fromRunnable(() -> {
			
			//local 환경에서는 bmon연동 안함
			if("local".equals(onProfile)) {
				log.debug("{}", "BMON Local Skip!");
				//return;
			}
	
			//bmon flag 처리
			if(!bmonTrtFlag) {
				log.debug("{}", "BMON Flag is false. Skip!");
				return; 
			}
			
			JSONObject bmonHeader = new JSONObject();
			bmonHeader.put("appName", Constants.APP_NAME);
			bmonHeader.put("svcName", request.getHeader(HeaderConstants.HEADER_ORI_URI));
			bmonHeader.put("fnName", "service");
			bmonHeader.put("globalNo", request.getHeader(HeaderConstants.HEADER_GLOBAL_NO));
			bmonHeader.put("chnlType", request.getHeader(HeaderConstants.HEADER_CHNL_TYPE));
			
			bmonHeader.put("trFlag", TrFlag);
			bmonHeader.put("trDate", DateUtil.Date_yyyyMMdd());
			bmonHeader.put("trTime", DateUtil.Date_HHmmssSSS());
			bmonHeader.put("clntIp", request.getHeader(HeaderConstants.HEADER_ORI_IP));
			bmonHeader.put("responseType", trtErrInfoDTO.responseType());
			bmonHeader.put("responseCode", trtErrInfoDTO.responseCode());
			bmonHeader.put("responseTitle", "");
			bmonHeader.put("responseBasc", trtErrInfoDTO.responseBasc());
			bmonHeader.put("responseDtal", trtErrInfoDTO.responseDtal());
			bmonHeader.put("userId", request.getHeader(HeaderConstants.HEADER_USER_ID));
			bmonHeader.put("orgId", request.getHeader(HeaderConstants.HEADER_ORG_ID));
			bmonHeader.put("srcId", apiSrcName);
			bmonHeader.put("lgDateTime", request.getHeader(HeaderConstants.HEADER_LG_DATE_TIME));
			bmonHeader.put("cmpnCd", request.getHeader(HeaderConstants.HEADER_CMPN_CD));
			bmonHeader.put("curHostId", StringUtil.getIPAddress());
					
			//header key/value로 변환
			StringBuilder headerStrBulder = new StringBuilder();
			for(Object key : bmonHeader.keySet()) {
				headerStrBulder.append(key)
								.append(this.HEADER_SEPARATOR)
								.append(bmonHeader.get((String) key))
								.append(this.LINE_FEED);
			}
	
			/*
			2025.04.28 json전문 연동은 bmon에서 마스킹 불가. Key:Value 형식으로 변경
			//body json String 변환
			String bodyString = "";
			try {
				ObjectMapper objMapper = new ObjectMapper();

				//T인경우 DTO만, R인경우 VO로 처리한다.
				if("T".equals(TrFlag)) {
					bodyString = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inDTO);
				} else {
					RequestStdVO<T> reqVO = new RequestStdVO<T>(trtErrInfoDTO, inDTO);
					bodyString = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reqVO);
				}
			} catch (JsonProcessingException e) {
				//BMON연동은 오류 처리 없음.
				log.error("BMON 메세지변환 오류 발생>{}", e.toString());
			}
			*/
			
			//body json Key:Value형태 문자열로 변환
			String bodyString;

			if("T".equals(TrFlag)) {
				ObjectMapper objMapper = new ObjectMapper();
				String a = "";
				try{
				a = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inDTO);
				}catch(Exception e){

				}


				bodyString = jsonToKeyValue(new JSONObject(a), "");
			} else {
				RequestStdVO<T> reqVO = new RequestStdVO<T>(trtErrInfoDTO, inDTO);
				bodyString = jsonToKeyValue(new JSONObject(reqVO), "");
			}

			
			log.debug("BMON 연동 시작. 입력헤더=[{}]", headerStrBulder.toString());
			log.debug("BMON 연동 시작. 입력전문=[{}]", bodyString);

			CommonPayloadCollector.sendPayloadKeyValue(Constants.LOG_POINT, headerStrBulder.toString(), bodyString);
			
		}).subscribeOn(Schedulers.boundedElastic()).then();
	}

	//JSONObject to Key:Value
	public static String jsonToKeyValue(JSONObject inJsonObj, String prefix) {

		StringBuilder result = new StringBuilder();
		Iterator<String> keys = inJsonObj.keys();

		while(keys.hasNext()) {
			String key = keys.next();
			Object value = inJsonObj.get(key);

			String currKey = prefix.isEmpty() ? key : prefix + "." + key;

			if(value instanceof JSONObject) {
				result.append(jsonToKeyValue((JSONObject) value, currKey));
			} else if(value instanceof JSONArray){
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

	public static String processJsonArray(JSONArray jsonArray, String prefix) {

		StringBuilder result = new StringBuilder();

		for(int i=0; i < jsonArray.length(); i++) {
			Object item = jsonArray.get(i);
			String currKey = prefix + "[" + i + "]";

			if(item instanceof JSONObject) {
				result.append(jsonToKeyValue((JSONObject) item, currKey));
			} else if(item instanceof JSONArray) {
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
