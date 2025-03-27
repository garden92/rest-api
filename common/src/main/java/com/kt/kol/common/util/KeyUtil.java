package com.kt.kol.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class KeyUtil {
	
	/**
	 * <pre>
	 * globalNo를 생성한다.
	 * </pre>
	 * 
	 * @param String
	 * @return N/A
	 */
	public static String makeGlobalNo(String userId) {
		
		String sGlobalNo = userId + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());                           
        
		/** globerNo 중복 발생방지 랜덤 숫자 추가 */                                                                         
		Random random = new Random();                                                                                          
		int rnd = random.nextInt(999999);                                                                                      
		sGlobalNo = String.format("%s%s", sGlobalNo, String.valueOf(rnd));                                                     
		/** globerNo 중복 발생방지 랜덤 숫자 추가 */                                                                         

		sGlobalNo = StringUtil.lpad(sGlobalNo, '0', 32);
		
		return sGlobalNo;
		
	}
}
