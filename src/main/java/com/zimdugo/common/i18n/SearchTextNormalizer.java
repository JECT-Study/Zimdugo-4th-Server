package com.zimdugo.common.i18n;

import java.text.Normalizer;
import java.util.Locale;

public final class SearchTextNormalizer {

    private SearchTextNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(normalized.length());
        normalized.codePoints()
            .filter(codePoint -> !Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint))
            .forEach(result::appendCodePoint);
        return result.toString();
    }
}
