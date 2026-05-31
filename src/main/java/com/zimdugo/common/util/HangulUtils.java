package com.zimdugo.common.util;

public class HangulUtils {

    private static final char HANGUL_BEGIN = 0xAC00;
    private static final char HANGUL_END = 0xD7AF;
    private static final int JUN_CNT = 21;
    private static final int JON_CNT = 28;

    private static final char[] CHOSEONG = {
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    private static final char[] JUNGSEONG = {
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };
    private static final char[] JONGSEONG = {
        '\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ',
        'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    public static String decompose(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= HANGUL_BEGIN && c <= HANGUL_END) {
                int base = c - HANGUL_BEGIN;
                int choseongIdx = base / (JUN_CNT * JON_CNT);
                int jungseongIdx = (base % (JUN_CNT * JON_CNT)) / JON_CNT;
                int jongseongIdx = base % JON_CNT;

                result.append(CHOSEONG[choseongIdx]);
                result.append(JUNGSEONG[jungseongIdx]);
                if (jongseongIdx > 0) {
                    result.append(JONGSEONG[jongseongIdx]);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
