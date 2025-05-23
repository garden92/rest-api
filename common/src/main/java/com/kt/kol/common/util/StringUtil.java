package com.kt.kol.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtil {
	
	/**
	 * <pre>
	 * 입력된 getter/setter Object의 값을 출력한다.
	 * </pre>
	 * 
	 * @param Object
	 * @return N/A
	 */
	@SuppressWarnings("rawtypes")
	public static String toString(Object obj) {
		
		ObjectMapper mapper = new ObjectMapper();
		try
		{
		
			if(obj.getClass().equals(ArrayList.class))
			{
				StringBuffer tempBuff = new StringBuffer();
				
				for(int i=0; i < ((List)obj).size(); i++)
				{
					String jsonStr 	= ToStringBuilder.reflectionToString(((List)obj).get(i), ToStringStyle.JSON_STYLE);
					Object jsonType = mapper.readValue(jsonStr.getBytes("UTF-8"), Object.class);
					
					tempBuff.append("\n").append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonType));
				}
				
				return tempBuff.toString();
			}
			else
			{
				String jsonStr 	= ToStringBuilder.reflectionToString(obj, ToStringStyle.JSON_STYLE);
				Object jsonType = mapper.readValue(jsonStr.getBytes("UTF-8"), Object.class);
				
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonType);
			}
			
		} catch (IOException e)
		{
			return "Object Logging convert error!!";
		}
	}
	
	/**
	 * <pre>
	 * 문자열 앞에 특정문자를 붙여서 정해진 길이 문자열로 만듦.
	 * 
	 * 사용 예)
	 * StringUtil.lpad("123", '0', 5) => "00123"
	 * StringUtil.lpad("123", '0', 1) => "123"
	 * StringUtil.lpad(null, '0', 1) => null
	 * </pre>
	 * 
	 * @param orgStr 변경할 String
	 * @param appender 나머지를 채울 문자
	 * @param length 새로 만들 String의 길이
	 * @return 새로 만든 String, 입력 문자열이 주어진 길이보다 큰 경우 입력 문자열, 입력 문자열이 null일 경우 null
	 */
	public static String lpad(final String orgStr, final char appender, final int length)
	{
		if (orgStr == null)
		{
			return null;
		}

		final int orgLen = orgStr.length();
		if (orgLen >= length)
		{
			return orgStr;
		} else {
			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < length - orgLen; i++)
			{
				sb.append(appender);
			}
			sb.append(orgStr);
			return sb.toString();
		}
	}

	/**
	 * <pre>
	 * 문자열 뒤에 특정문자를 붙여서 정해진 길이 문자열로 만듦.
	 * 
	 * 사용 예)
	 * StringUtil.rpad("abc", ' ', 5) => "abc  "
	 * StringUtil.rpad("abc", '0', 1) => "abc"
	 * StringUtil.rpad(null, '0', 1) => null
	 * </pre>
	 * 
	 * @param str 변경할 String
	 * @param appender 나머지를 채울 문자
	 * @param length 새로 만들 String의 길이
	 * @return 새로 만든 String, 입력 문자열이 주어진 길이보다 큰 경우 입력 문자열, 입력 문자열이 null일 경우 null
	 */
	public static String rpad(final String str, final char appender, final int length)
	{
		if (str == null)
		{
			return null;
		}

		final int orgLen = str.length();
		if (orgLen >= length)
		{
			return str;
		} else {
			final StringBuffer sb = new StringBuffer();
			sb.append(str);
			for (int i = 0; i < length - orgLen; i++)
			{
				sb.append(appender);
			}
			return sb.toString();
		}
	}
	
	/**
	 * <pre>
	 * 현재 IP return
	 * </pre>
	 * 
	 * @param N/A
	 * @return 새로 만든 String, 입력 문자열이 주어진 길이보다 큰 경우 입력 문자열, 입력 문자열이 null일 경우 null
	 * @throws UnknownHostException 
	 */
	public static String getIPAddress() {
		
		String ip;
		
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			ip = inetAddress.getHostAddress();
		} catch (Exception e) {
			ip = "";
		}
		
		return ip;
		
	}
	
	/**
	 * <pre>
	 * 문자열이 Null 또는 Empty String(빈 문자열)이면 true, 아니면 false를 반환한다.
	 * 
	 * 사용 예)
	 * String str = null;
	 * StringUtil.isNull(str) => true
	 * 
	 * String str2 = "";
	 * StringUtil.isNull(str2) => true
	 * </pre>
	 * @param str 입력한 문자열
	 * @return (true or false)
	 */
	public static boolean isNull(String str) {
		return (str == null) || (str.trim().equals(""));
	}
	
	/**
	 * <pre>
	 * 입력된 Exception객체를 String으로 반환한다.
	 * </pre>
	 * 
	 * @param Exception e
	 * @return N/A
	 */
	public static String printExceptionStack(Exception e) {
		StringWriter errStr = new StringWriter();
		e.printStackTrace(new PrintWriter(errStr));
		return errStr.toString();
	}
	
}
