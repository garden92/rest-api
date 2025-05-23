package com.kt.kol.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Util {
    public static void main(String[] args) {
        String[] texts = {
            "파란색 무지개",
            "달콤한 바람",
            "춤추는 나무",
            "웃는 고양이",
            "별빛 속삭임",
            "바다의 노래",
            "구름 위의 성",
            "숲속의 비밀",
            "하늘을 나는 물고기",
            "시간의 모래시계",
            "꿈꾸는 돌고래",
            "은빛 강물",
            "노래하는 꽃",
            "바람의 그림자",
            "빛나는 별똥별",
            "숲의 속삭임",
            "달빛 아래 춤",
            "하늘의 미소",
            "바다의 속삭임",
            "구름 위의 산책",
            "별빛의 춤",
            "바람의 노래",
            "숲속의 모험",
            "하늘을 나는 배",
            "시간의 여행자",
            "꿈꾸는 나비",
            "은빛 달빛",
            "노래하는 새",
            "바람의 이야기",
            "빛나는 해변",
            "숲의 노래",
            "달빛의 미소",
            "하늘의 속삭임",
            "바다의 모험",
            "구름 위의 꿈",
            "별빛의 이야기",
            "바람의 춤",
            "숲속의 여행",
            "하늘을 나는 새",
            "시간의 마법사",
            "꿈꾸는 별",
            "은빛 바다",
            "노래하는 강",
            "바람의 미소",
            "빛나는 숲",
            "숲의 이야기",
            "달빛의 노래",
            "하늘의 모험",
            "바다의 춤",
            "구름 위의 이야기",
            "별빛의 모험",
            "바람의 속삭임",
            "숲속의 마법",
            "하늘을 나는 자동차",
            "짜장면 맛있어",
            "짬뽕도 끝내줘",
            "마법의 숲속"
        };

        for (int i = 0; i < texts.length; i++) {
            String hash = getSHA256Hash(texts[i]);
            System.out.println((i + 1) + ". Original: " + texts[i]);
            System.out.println("   SHA-256 Hash: " + hash);
            System.out.println();
        }
    }

    public static String getSHA256Hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}