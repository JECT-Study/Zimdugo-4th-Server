package com.zimdugo.common.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SearchTextNormalizerTest {

    @Test
    void normalizesWithNfkcLowercaseAndWhitespaceRemoval() {
        assertThat(SearchTextNormalizer.normalize(" Ｓｅｏｕｌ\tStation\n"))
            .isEqualTo("seoulstation");
    }

    @Test
    void removesUnicodeSpaceCharacters() {
        assertThat(SearchTextNormalizer.normalize("서울\u00a0역")).isEqualTo("서울역");
    }

    @Test
    void normalizesNullToEmptyString() {
        assertThat(SearchTextNormalizer.normalize(null)).isEmpty();
    }
}
