package com.kt.kol.common.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtil {
    
    /**
     * yyyyMMdd return
     * @return yyyyMMdd
     */
    fun dateYyyyMMdd(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }
    
    /**
     * HHmmssSSS return
     * @return HHmmssSSS
     */
    fun dateHHmmssSSS(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS"))
    }
    
    /**
     * yyyyMMddHHmmss return
     * @return yyyyMMddHHmmss
     */
    fun dateYyyyMMddHHmmss(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    }
}